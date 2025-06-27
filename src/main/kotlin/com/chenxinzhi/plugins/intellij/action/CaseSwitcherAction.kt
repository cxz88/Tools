package com.chenxinzhi.plugins.intellij.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel
import com.intellij.openapi.project.Project

class CaseSwitcherAction : AnAction() {
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
        val styles = listOf(
            ::toCamelCase, ::toSnakeCase, ::toUpperSnakeCase, ::toPascalCase
        )

        val normalized = normalizeToWords(input)
        val currentIndex = styles.indexOfFirst { it(normalized) == input }
        val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % styles.size
        return styles[nextIndex](normalized)
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