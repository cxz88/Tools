package com.chenxinzhi.plugins.intellij.utils
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.TranslationCacheService
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
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files


fun localizeLiteralArgsUsingPsi(
    project: Project,
    fqcnOfException: String = "java.lang.RuntimeException",
    i18nUtilFqn: String = "org.springblade.core.utils.MessageUtils",
    bundleBaseName: String = "message",
    resourceDirRelative: String = "src/main/resources/i18n",
    modulePath: String = "",
    module: Module
) {
    runLocalizationTask(project) { progressIndicator ->

        progressIndicator.isIndeterminate = false
        progressIndicator.text = LanguageBundle.messagePointer("tran.scanning").get()
        val psiClassList =
            runReadAction {
                listOf(
                    JavaPsiFacade.getInstance(project).findClass(fqcnOfException, GlobalSearchScope.allScope(project)),
                    JavaPsiFacade.getInstance(project).findClass("org.springblade.core.tool.api.R", GlobalSearchScope.allScope(project))

                )


            }.filterNotNull()

        if (progressIndicator.isCanceled) throw CancellationException("")
        // 查找所有引用（包括 new 表达式）
        val refs =
            runReadAction {
              psiClassList.flatMap {psiClass->
                  val search = ClassInheritorsSearch.search(psiClass, GlobalSearchScope.allScope(project), true)
                  val allSubClasses: MutableCollection<PsiClass?> = search.findAll().toMutableList()
                      .apply {
                          add(psiClass)
                      }
                  allSubClasses.flatMap {
                      it?.let { element -> ReferencesSearch.search(element, GlobalSearchScope.moduleScope(module)) }
                          ?.findAll() ?: emptyList()
                  }
              }


            }

        if (progressIndicator.isCanceled) throw CancellationException("")
        if (refs.isEmpty()) return@runLocalizationTask {}

        val defaultFile = File(modulePath, "$resourceDirRelative/$bundleBaseName.properties")
        val koFile = File(modulePath, "$resourceDirRelative/${bundleBaseName}_ko_KR.properties")
        val zhFile = File(modulePath, "$resourceDirRelative/${bundleBaseName}_zh_CN.properties")
        if (progressIndicator.isCanceled) throw CancellationException("")
        val defaultProps =
            loadPropertiesReadable(defaultFile, project)

        if (progressIndicator.isCanceled) throw CancellationException("")
        val koProps =
            loadPropertiesReadable(koFile, project)

        val zhProps =
            loadPropertiesReadable(zhFile, project)


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

        progressIndicator.text = LanguageBundle.messagePointer("tran.translating.name").get()
        progressIndicator.fraction = 0.0
        progressIndicator.text2 = null
        val size2 = literalPairs.size
        // 获取翻译缓存服务
        val cacheService = TranslationCacheService.getInstance(project)
        // 先尝试复用已有 properties 中的 key（value 匹配）
        literalPairs = literalPairs.map { it.first }.zip(map).mapIndexed { index, (expr, v) ->
            val existingKey = defaultProps.entries.firstOrNull { it.value == v }?.key
            expr to (existingKey ?: textToKey.getOrPut(v) {
                progressIndicator.text2 = "${LanguageBundle.messagePointer("tran.translating.name").get()} $v"
                progressIndicator.fraction = (index.toDouble() / size2)
                val translation = cacheService.getTranslation(v)
                if (translation == null) {
                   ""
                }else{
                    generateKeyFromText(
                        runReadAction { PsiTreeUtil.getParentOfType(expr, PsiClass::class.java)?.qualifiedName }
                            ?: "", translation.first
                    )
                }

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
            if (!zhProps.containsKey(key)) {
                zhProps[key] = text
            }
        }
        progressIndicator.fraction = 0.4
        if (needTranslation.isNotEmpty()) {
            // 先检查缓存中是否有翻译
            needTranslation.forEach { (key, text) ->
                val cachedTranslation = cacheService.getTranslation(text)
                if (cachedTranslation != null) {
                    // 使用缓存的翻译
                    koProps[key] = cachedTranslation.second.second
                    defaultProps[key] = cachedTranslation.second.first
                }
                if ((cachedTranslation == null)) {
                    koProps.remove(key)
                    defaultProps.remove(key)
                    zhProps.remove(key)
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
                    literalPairs.filter {
                        koProps[it.second]?.isNotBlank() ?: false
                    }.forEachIndexed { index, (literal, key) ->
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
                    val findFileByIoFileKo =
                        LocalFileSystem.getInstance().findFileByIoFile(koFile)
                    val findFileByIoFileZh =
                        LocalFileSystem.getInstance().findFileByIoFile(zhFile)
                    // 保存 properties 文件
                    findFileByIoFile
                        ?.let {
                            savePropertiesReadable(project, it, defaultProps)
                        }
                    findFileByIoFileKo
                        ?.let { savePropertiesReadable(project, it, koProps) }
                    findFileByIoFileZh
                        ?.let { savePropertiesReadable(project, it, zhProps) }
                    // 刷新 VFS 以便 IDEA 看到文件变化
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(defaultFile)
                        ?.let { VfsUtil.markDirtyAndRefresh(false, false, false, it) }
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(koFile)
                        ?.let { VfsUtil.markDirtyAndRefresh(false, false, false, it) }
                    LocalFileSystem.getInstance().refreshAndFindFileByIoFile(zhFile)
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
        runReadAction { PsiManager.getInstance(project).findFile(vFile) }
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
                ?: Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)
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
    // 使用拼音作为key
    // 如果拼音为空，使用原来的逻辑
    if (text.isNotBlank()) {
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

    return text
}


fun runLocalizationTask(project: Project, task: suspend (ProgressIndicator) -> ((ProgressIndicator) -> Unit)) {

    ProgressManager.getInstance().run(object : Task.Modal(
        project,
        "${LanguageBundle.messagePointer("tran.app.scan.processing").get()} RuntimeException...",
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
                    "${LanguageBundle.messagePointer("tran.replacing.translate").get()} RuntimeException",
                    project,
                    null
                ) { indicator: ProgressIndicator ->
                    runBlocking {
                        b(indicator)
                    }
                }
        }
    })

}





