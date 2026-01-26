package com.chenxinzhi.plugins.intellij.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.TranService
import com.chenxinzhi.plugins.intellij.services.TranslationCacheService
import com.chenxinzhi.plugins.intellij.utils.*
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.fileChooser.FileChooserFactory
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiNewExpression
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.search.searches.ReferencesSearch
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.io.File
import java.nio.file.Paths

@Composable
fun Lan(project: Project) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var moduleList by remember {
                mutableStateOf(
                    listOf(
                        GenData(
                            "", AllIconsKeys.Nodes.Module
                        )
                    )
                )
            }
            var enabled by remember {
                mutableStateOf(
                    false
                )
            }
            LaunchedEffect(Unit) {
                DumbService.getInstance(project).runWhenSmart {
                    moduleList = project.getAllModules().map {
                        GenData(
                            it.name,
                            AllIconsKeys.Nodes.Module,
                            (Paths.get(it.moduleFilePath).parent ?: "").toString(),
                            it
                        )
                    }.apply {
                        ifEmpty {
                            listOf(
                                GenData(
                                    "", AllIconsKeys.Nodes.Module
                                )
                            )
                        }
                    }
                    enabled = true
                }

            }
            var selectedIndex by remember { mutableIntStateOf(0) }
            ComboList(moduleList, selectedIndex, true) {
                selectedIndex = it
            }
            Spacer(modifier = Modifier.height(16.dp))
            val settingsService = remember { TranService.getInstance(project) }
            val appSecret = textFieldState()
            val appKey = textFieldState()
            val saveSettings = remember {
                {
                    settingsService.state.appKey = appSecret.text.toString()
                    settingsService.state.appId = appKey.text.toString()
                }
            }
            var first by remember { mutableStateOf(true) }
            LaunchedEffect(appKey.text, appSecret.text) {
                if (first) {
                    return@LaunchedEffect
                }
                saveSettings()
            }
            LaunchedEffect(Unit) {
                appSecret.setTextAndPlaceCursorAtEnd(settingsService.state.appKey)
                appKey.setTextAndPlaceCursorAtEnd(settingsService.state.appId)
                first = false
            }

            var isImport by remember { mutableStateOf(false) }
            // 导出和导入按钮
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(enabled = enabled, onClick = {
                    val data = moduleList[selectedIndex]
                    data.module?.let { module ->
                        exportTranslationsToExcel(project, module)

                    }
                }) {
                    Text(LanguageBundle.messagePointer("tran.excel.export").get())
                }

                OutlinedButton(enabled = enabled, onClick = {
                    importTranslationsFromExcel(project) {
                        isImport = true
                    }
                }) {
                    Text(
                        "${
                            LanguageBundle.messagePointer("tran.excel.import").get()
                        } ${if (isImport) LanguageBundle.messagePointer("tran.excel.import.success").get() else ""}"
                    )

                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            DefaultButton(enabled = enabled, onClick = {
                val data = moduleList[selectedIndex]
                data.module?.let {
                    localizeLiteralArgsUsingPsi(
                        project,
                        modulePath = data.other,
                        module = it
                    )
                }
            }) {
                Text(LanguageBundle.messagePointer("tran.translate").get())
            }
        }
    }

}

/**
 * 导出翻译内容到Excel
 */
private fun exportTranslationsToExcel(project: Project, module: Module) {
    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
        .withTitle(LanguageBundle.message("tran.excel.select.export.dir"))
        .withDescription(LanguageBundle.message("tran.excel.select.export.dir.desc"))

    val chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, project, null)
    val selectedFiles =
        chooser.choose(project)

    ProgressManager.getInstance().run(object : Task.Modal(
        project,
        "${LanguageBundle.messagePointer("tran.app.scan.processing").get()} RuntimeException...",
        true
    ) {
        override fun run(p0: ProgressIndicator) {
            if (selectedFiles.isNotEmpty()) {
                val selectedDir = File(selectedFiles[0].path)

                // 收集需要翻译的中文字符串
                val chineseTexts = mutableListOf<String>()

                DumbService.getInstance(project).runReadActionInSmartMode<Unit> {
                    val psiClassList =
                        runReadAction {
                            listOf(
                                JavaPsiFacade.getInstance(project)
                                    .findClass(
                                        "java.lang.RuntimeException",
                                        GlobalSearchScope.allScope(project)
                                    ),
                                JavaPsiFacade.getInstance(project)
                                    .findClass("org.springblade.core.tool.api.R", GlobalSearchScope.allScope(project))
                            )
                        }.filterNotNull()

                    runReadAction {


                        val allSubClasses = psiClassList.flatMap { psiClass ->
                            val search =
                                ClassInheritorsSearch.search(psiClass, GlobalSearchScope.allScope(project), true)
                            search.findAll()
                        }
                        val refs = allSubClasses.flatMap {
                            it?.let { element ->
                                ReferencesSearch.search(
                                    element,
                                    GlobalSearchScope.moduleScope(module)
                                )
                            }
                                ?.findAll() ?: emptyList()
                        }
                        refs.forEach { ref ->
                            val element = ref.element
                            val parent = element.parent

                            if (parent is PsiNewExpression) {
                                val argList = parent.argumentList
                                val expressions = argList?.expressions ?: emptyArray()

                                for (expr in expressions) {
                                    if (expr is PsiLiteralExpression) {
                                        val value = expr.value as? String
                                        if (!value.isNullOrBlank() && !chineseTexts.contains(value)) {
                                            chineseTexts.add(value)
                                        }
                                    }
                                }
                            }
                        }

                    }
                }

                if (chineseTexts.isEmpty()) {
//            project.notifyInfo("未找到需要翻译的内容")
                    return
                }

                // 导出到Excel
                val outputFile = File(selectedDir, "${module.name}_${System.currentTimeMillis()}.xlsx")
                try {
                    ExcelUtils.exportToExcel(chineseTexts, outputFile)
//            project.notifyInfo("成功导出 ${chineseTexts.size} 条内容到 ${outputFile.name}")
                } catch (e: Exception) {
//            project.notifyInfo("导出失败: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    })
}

/**
 * 从Excel导入翻译内容
 */
private fun importTranslationsFromExcel(project: Project, fallBack: () -> Unit) {
    val descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor("xlsx")
        .withTitle(LanguageBundle.message("tran.excel.select.import.file"))
        .withDescription(LanguageBundle.message("tran.excel.select.import.file.desc"))

    val chooser = FileChooserFactory.getInstance().createFileChooser(descriptor, project, null)
    val selectedFiles = chooser.choose(project)

    if (selectedFiles.isNotEmpty()) {
        val selectedFile = File(selectedFiles[0].path)

        try {
            val translations = ExcelUtils.importFromExcel(selectedFile)

            if (translations.isEmpty()) {
                project.notifyError("未找到有效的翻译内容")
                return
            }

            // 将翻译内容存入缓存
            val cacheService = TranslationCacheService.getInstance(project)
            cacheService.clear() // 清空之前的缓存
            cacheService.addTranslations(translations)
            fallBack()
            project.notifySuccess("成功导入 ${translations.size} 条翻译内容")
        } catch (e: Exception) {
            project.notifyError("导入失败: ${e.message}")
            e.printStackTrace()
        }
    }
}