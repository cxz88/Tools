package com.chenxinzhi.plugins.intellij.action

import com.chenxinzhi.plugins.intellij.script.GroovyScriptEvaluator
import com.chenxinzhi.plugins.intellij.settings.NamingStyleSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project
import kotlinx.coroutines.runBlocking

class CaseSwitcherAction : AnAction() {
    private val settings = NamingStyleSettings.getInstance()
    private val scriptEvaluator = GroovyScriptEvaluator()

    override fun actionPerformed(e: AnActionEvent) {
        val project: Project = e.project ?: return
        val editor: Editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val selectionModel: SelectionModel = editor.selectionModel
        if (!selectionModel.hasSelection()) return
        val selectedText = selectionModel.selectedText ?: return
        val switched = toggleNamingStyle(selectedText)

        WriteCommandAction.runWriteCommandAction(project) {
            val start = selectionModel.selectionStart
            val end = selectionModel.selectionEnd
            editor.document.replaceString(start, end, switched)
        }
    }

    fun toggleNamingStyle(input: String): String {
        // 获取配置的命名风格
        val styles = settings.getSortedStyles()
        if (styles.isEmpty()) {
            return input
        }

        val normalized = normalizeToWords(input)

        // 查找当前的命名风格
        var currentIndex = -1
        for (i in styles.indices) {
            val result = executeNamingStyle(styles[i], normalized)
            if (result == input) {
                currentIndex = i
                break
            }
        }

        // 获取下一个命名风格
        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % styles.size
        return executeNamingStyle(styles[nextIndex], normalized)
    }

    /**
     * 执行命名风格转换
     */
    private fun executeNamingStyle(style: com.chenxinzhi.plugins.intellij.settings.NamingStyle, input: String): String {
        // 使用脚本执行转换
        if (style.scriptContent.isNotBlank()) {
            return try {
                runBlocking {
                    scriptEvaluator.evaluate(style.scriptContent, mapOf("input" to input)) ?: input
                }
            } catch (_: Exception) {
                input
            }
        }

        // 如果没有脚本内容，返回原始输入
        return input
    }

    fun normalizeToWords(input: String): String {
        val snake = input.replace(Regex("([a-z])([A-Z])"), "$1_$2").replace("-", "_").replace(" ", "_")
        return snake.lowercase()
    }

}