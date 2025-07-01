package com.chenxinzhi.plugins.intellij.action

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
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
        // 优先使用脚本（总是应该使用脚本）
        if (style.scriptContent.isNotBlank()) {
            try {
                return runBlocking {
                    scriptEvaluator.evaluate(style.scriptContent, mapOf("input" to input)) ?: input
                }
            } catch (e: Exception) {
                com.intellij.openapi.diagnostic.Logger.getInstance(CaseSwitcherAction::class.java)
                    .error("脚本执行错误: ${e.message}", e)
                return input
            }
        }

        // 备用方法（如果脚本为空）
        return when (style.methodName) {
            "toCamelCase" -> toCamelCase(input)
            "toSnakeCase" -> toSnakeCase(input)
            "toUpperSnakeCase" -> toUpperSnakeCase(input)
            "toPascalCase" -> toPascalCase(input)
            else -> input
        }
    }

    fun normalizeToWords(input: String): String {
        val snake = input.replace(Regex("([a-z])([A-Z])"), "$1_$2").replace("-", "_").replace(" ", "_")
        return snake.lowercase()
    }

    // 转为 camelCase
    fun toCamelCase(input: String): String = input.split("_").mapIndexed { i, part ->
        if (i == 0) part else part.replaceFirstChar { it.uppercase() }
    }.joinToString("")

    // 转为 snake_case
    fun toSnakeCase(input: String): String = input.split("_").joinToString("_") { it.lowercase() }

    // 转为 UPPER_SNAKE_CASE
    fun toUpperSnakeCase(input: String): String = input.split("_").joinToString("_") { it.uppercase() }

    // 转为 PascalCase
    fun toPascalCase(input: String): String =
        input.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
}