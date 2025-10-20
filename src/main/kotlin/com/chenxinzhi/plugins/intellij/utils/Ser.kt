import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.utils.notifyError
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.google.common.base.CaseFormat
import com.intellij.lang.properties.psi.PropertiesFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.impl.ApplicationImpl
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.youdao.aicloud.translate.TranslateDemo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.function.Consumer


fun localizeLiteralArgsUsingPsi(
    project: Project,
    fqcnOfException: String = "org.springblade.core.log.exception.ServiceException",
    i18nUtilFqn: String = "org.springblade.core.utils.MessageUtils",
    bundleBaseName: String = "message",
    resourceDirRelative: String = "src/main/resources/i18n",
    modulePath: String = "",
    module: Module,
    appSecret: String = "",
    appKey: String = ""
) {
    runLocalizationTask(project) { progressIndicator ->

        progressIndicator.isIndeterminate = false
        progressIndicator.text = LanguageBundle.messagePointer("tran.scanning").get()
        val psiClass =
            runReadAction {
                JavaPsiFacade.getInstance(project).findClass(fqcnOfException, GlobalSearchScope.allScope(project))
            }
                ?: return@runLocalizationTask {}
        if (progressIndicator.isCanceled) throw CancellationException("")
        // 查找所有引用（包括 new 表达式）
        val refs =
            runReadAction {
                ReferencesSearch.search(psiClass, GlobalSearchScope.moduleScope(module)).findAll()
            }

        if (progressIndicator.isCanceled) throw CancellationException("")
        if (refs.isEmpty()) return@runLocalizationTask {}

        val defaultFile = File(modulePath, "$resourceDirRelative/$bundleBaseName.properties")
        val koFile = File(modulePath, "$resourceDirRelative/${bundleBaseName}_ko_KR.properties")
        if (progressIndicator.isCanceled) throw CancellationException("")
        val defaultProps =
            loadPropertiesReadable(defaultFile, project)

        if (progressIndicator.isCanceled) throw CancellationException("")
        val koProps =
            loadPropertiesReadable(koFile, project)


        // 收集将要替换的字面量节点与对应 key
        var literalPairs = mutableListOf<Pair<PsiLiteralExpression, String>>() // (literalNode, key)
        val textToKey = mutableMapOf<String, String>() // 避免同文本生成多个 key
        val f = refs.size
        progressIndicator.fraction = 0.0
        progressIndicator.text = LanguageBundle.messagePointer("tran.translating.name").get()
        progressIndicator.text2 = null

        refs.forEachIndexed { index, ref ->
            if (progressIndicator.isCanceled) throw CancellationException("")
            val element = ref.element
            val parent = element.parent
            progressIndicator.fraction = index.toDouble() / f
            // 只关心 PsiNewExpression（new ...(...)）
            if (parent is PsiNewExpression) {
                val argList = runReadAction { parent.argumentList } ?: return@forEachIndexed
                val expressions =
                    runReadAction { argList.expressions }

                val text =
                    runReadAction { parent.text }

                progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.scanning").get()} $text"
                // 遍历每个参数，找到纯字符串字面量
                for (expr in expressions) {
                    if (expr is PsiLiteralExpression) {
                        val v = runReadAction { expr.value } as? String ?: continue
                        if (v.isBlank()) continue
                        literalPairs.add(expr to v)
                    }
                }
            }

        }
        val map = literalPairs.map { it.second }
        val result =
            chunkConcat(map, 500, appKey, appSecret, progressIndicator, "en", project)
                .map {
                    it?.lowercase()
                        ?.split("[^a-z0-9]+".toRegex())  // 以非字母数字分割
                        ?.filter { str -> str.isNotEmpty() }     // 去掉空串
                        ?.joinToString(".") ?: ""
                }
        progressIndicator.text = LanguageBundle.messagePointer("tran.translating.name").get()
        progressIndicator.fraction = 0.0
        progressIndicator.text2 = null
        val size2 = literalPairs.size
        // 先尝试复用已有 properties 中的 key（value 匹配）
        literalPairs = literalPairs.map { it.first }.zip(result).mapIndexed { index, (expr, v) ->
            val existingKey = defaultProps.entries.firstOrNull { it.value == v }?.key
            expr to (existingKey ?: textToKey.getOrPut(v) {
                progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.translating.name").get()} $v"
                progressIndicator.fraction = (index.toDouble() / size2)
                generateKeyFromText(
                    runReadAction { PsiTreeUtil.getParentOfType(expr, PsiClass::class.java)?.qualifiedName }
                        ?: "", v
                )
            })
        }.toMutableList()
        if (literalPairs.isEmpty()) return@runLocalizationTask {}
        // 在内存中保证 properties 包含这些 key（写默认中文；ko 保留空占位）
        progressIndicator.fraction = 0.0
        progressIndicator.text = LanguageBundle.messagePointer("tran.translating.ko").get()
        progressIndicator.text2 = null
        val needTranslation = mutableMapOf<String, String>()
        val size1 = literalPairs.size
        literalPairs.forEachIndexed { index, (literal, key) ->
            progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.processing").get()} $key"
            progressIndicator.fraction = (index / size1) * 0.4
            if (progressIndicator.isCanceled) throw CancellationException("")
            val text = runReadAction { literal.value } as? String ?: return@forEachIndexed
            if (!defaultProps.containsKey(key)) defaultProps[key] = text
            // 请求api服务
            if (!koProps.containsKey(key)) {
                koProps[key] = text
                needTranslation[key] = text
            }
        }
        progressIndicator.fraction = 0.4
        if (needTranslation.isNotEmpty()) {
            val result =
                chunkConcat(
                    needTranslation.values.toList(),
                    appKey = appKey,
                    appSecret = appSecret,
                    progressIndicator = progressIndicator,
                    project = project
                )
            val size = needTranslation.keys.size
            needTranslation.keys.forEachIndexed { index, key ->
                if (progressIndicator.isCanceled) throw CancellationException("")
                result[index]?.let {
                    progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.translating").get()} $it"
                    progressIndicator.fraction = (index.toDouble() / size) * 0.6
                    koProps[key] = it
                }
            }
        }
        progressIndicator.fraction = 1.0
        // 执行替换与写文件必须在 write command 中
        return@runLocalizationTask { progressIndicator ->
            progressIndicator.isIndeterminate = false
            progressIndicator.text = LanguageBundle.messagePointer("tran.replacing.translate").get()
            progressIndicator.fraction = 0.0
            progressIndicator.text2 = null
            runWriteCommandAction(project) {
                CommandProcessor.getInstance().markCurrentCommandAsGlobal(project)
                try {
                    val factory = JavaPsiFacade.getInstance(project).elementFactory
                    val f1 = literalPairs.size
                    literalPairs.forEachIndexed { index, (literal, key) ->
                        if (progressIndicator.isCanceled) {
                            return@forEachIndexed
                        }
                        val text = literal.text
                        progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.replacing").get()} $text"
                        progressIndicator.fraction = index.toDouble() / f1
                        // 只替换字面量节点为 I18nUtil 全限定名调用（避免处理 import）
                        val replacement = "$i18nUtilFqn.getMessage(\"$key\")"
                        literal.parent.let {
                            val newExpr = factory.createExpressionFromText(replacement, literal)
                            // 仅替换字面量节点
                            literal.replace(newExpr)
                            // 3. 自动缩短类全名
                            val javaCodeStyleManager = JavaCodeStyleManager.getInstance(project)
                            javaCodeStyleManager.shortenClassReferences(it)

                        }
                    }
                    val findFileByIoFile =
                        LocalFileSystem.getInstance().findFileByIoFile(defaultFile)
                    val findFileByIoFile1 =
                        LocalFileSystem.getInstance().findFileByIoFile(koFile)
                    // 保存 properties 文件
                    findFileByIoFile
                        ?.let {
                            savePropertiesReadable(project, it, defaultProps)
                        }

                    findFileByIoFile1
                        ?.let { savePropertiesReadable(project, it, koProps) }
                    // 刷新 VFS 以便 IDEA 看到文件变化
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(defaultFile)
                        ?.let { VfsUtil.markDirtyAndRefresh(false, false, false, it) }
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(koFile)
                        ?.let { VfsUtil.markDirtyAndRefresh(false, false, false, it) }
                    progressIndicator.text2 = LanguageBundle.messagePointer("tran.indexing").get()
                } finally {

                }
            }

        }
    }
}

/** 以可读 UTF-8 格式加载 properties（如果文件不存在返回空 map） */
private fun loadPropertiesReadable(file: File, project: Project): MutableMap<String, String> {
    val map = LinkedHashMap<String, String>()
    if (!file.exists()) return map
    val vFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file) ?: return map
    val psiFile =
        PsiManager.getInstance(project).findFile(vFile)
            ?: return map

    if (psiFile is PropertiesFile) {
        runReadAction {
            for (prop in psiFile.properties) {
                val key = prop.key ?: prop.name ?: continue
                val value = prop.value ?: ""
                map[key] = value
            }
        }

        return map
    }
    val doc = FileDocumentManager.getInstance().getDocument(vFile)
    val lines =
        runReadAction {
            doc?.text?.lines()
                ?: java.nio.file.Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
        }



    for (line in lines) {
        val l = line.trim()
        if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue
        val idx = l.indexOf('=')
        val k = l.take(idx).trim()
        val v = l.substring(idx + 1).trim()
        map[k] = v
    }


    return map
}

/** 使用 PSI 写入 properties 文件的内容；支持撤销操作 */
private fun savePropertiesReadable(project: Project, virtualFile: VirtualFile, map: Map<String, String>) {
    val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
    if (psiFile != null && psiFile.isWritable) {
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
        if (document != null) {
            val contentBuilder = StringBuilder()
            contentBuilder.append("# 以下由工具自动生成\n")
            map.forEach { (key, value) ->
                contentBuilder.append("$key=$value\n")
            }
            document.setText(contentBuilder.toString())
            PsiDocumentManager.getInstance(project).commitDocument(document)
        }

    }
}

private fun generateKeyFromText(fqcn: String, text: String): String {
    val removeSuffix =
        fqcn.substringAfter("impl.").substringAfter("controller.").substringAfter("util.").substringAfter("utils.")
            .removeSuffix("Impl").removeSuffix("Controller").removeSuffix("Util").removeSuffix("Service")
            .removeSuffix("Utils").replace(".", "_")

    val string = when {
        removeSuffix.first().isUpperCase() -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, removeSuffix)
        else -> CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, removeSuffix)
    }
    val replace = string.replace(Regex("_+")) { _ ->
        "."
    }

    return "$replace.$text"
}


fun runLocalizationTask(project: Project, task: suspend (ProgressIndicator) -> ((ProgressIndicator) -> Unit)) {

    ProgressManager.getInstance().run(object : Task.Modal(
        project,
        "${LanguageBundle.messagePointer("tran.app.scan.processing").get()} ServiceException...",
        true
    ) {
        var b: ((ProgressIndicator) -> Unit) = {}
        override fun run(indicator: ProgressIndicator) {
            try {
                runBlocking {
                    try {
                        b = task(indicator)
                    } finally {
                    }


                }


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun onFinished() {
            (ApplicationManager.getApplication() as ApplicationImpl)
                .runWriteActionWithCancellableProgressInDispatchThread(
                    "${LanguageBundle.messagePointer("tran.replacing.translate").get()} ServiceException",
                    project,
                    null,
                    Consumer { indicator: ProgressIndicator ->
                        runBlocking {
                            b(indicator)
                        }
                    })
        }
    })

}


suspend fun chunkConcat(
    strings: List<String>,
    maxLength: Int = 500,
    appKey: String = "",
    appSecret: String = "",
    progressIndicator: ProgressIndicator,
    to: String = "ko",
    project: Project
): List<String?> = coroutineScope {
    val toList = strings.chunked(maxLength).toList()
    val f1 = toList.size
    toList.mapIndexed { index, text ->
        val message = TranslateDemo.tran(text.toTypedArray(), "zh-CHS", to, appKey, appSecret)
        progressIndicator.fraction = index.toDouble() / f1
        val jacksonObjectMapper = jacksonObjectMapper()
        val readValue = try {
            jacksonObjectMapper.readValue(message, Map::class.java)
        } catch (_: Exception) {
            project.notifyError(LanguageBundle.messagePointer("tran.translating.err").get())
            throw CancellationException()
        }
        (readValue?.let {
            it.let { map ->
                map["translateResults"]?.let { it1 ->
                    if (it1 is List<*>) {
                        it1.map { it2 ->
                            val get = (it2 as? Map<*, *>)?.get("translation")
                            get.toString()
                        }
                    } else listOf(it1 as? String)
                }
            }
        } ?: throw CancellationException().apply {
            project.notifyError(LanguageBundle.messagePointer("tran.translating.err").get())
        }).apply {
            if (f1 == 1) {
                return@apply
            }
            delay(1000)
        }

    }.flatMap { it }.toList()
}


