package com.chenxinzhi.plugins.intellij.script

/**
 * 命名风格预览工具类
 * 
 * @author chenxinzhi
 * @date 2025-07-01
 */
object NamingStylePreview {
    /**
     * 预览示例文本
     */
    private val previewTexts = listOf(
        "user_name",
        "first_name",
        "DATE_OF_BIRTH",
        "HttpRequestHandler"
    )

    /**
     * 获取预览文本
     */
    fun getPreviewText(index: Int = 0): String {
        return previewTexts[index % previewTexts.size]
    }

    /**
     * 转为 camelCase
     */
    fun toCamelCase(input: String): String {
        val normalized = normalizeToWords(input)
        return normalized.split("_").mapIndexed { i, part ->
            if (i == 0) part else part.replaceFirstChar { it.uppercase() }
        }.joinToString("")
    }

    /**
     * 转为 snake_case
     */
    fun toSnakeCase(input: String): String {
        val normalized = normalizeToWords(input)
        return normalized.split("_").joinToString("_") { it.lowercase() }
    }

    /**
     * 转为 UPPER_SNAKE_CASE
     */
    fun toUpperSnakeCase(input: String): String {
        val normalized = normalizeToWords(input)
        return normalized.split("_").joinToString("_") { it.uppercase() }
    }

    /**
     * 转为 PascalCase
     */
    fun toPascalCase(input: String): String {
        val normalized = normalizeToWords(input)
        return normalized.split("_").joinToString("") { it.replaceFirstChar { c -> c.uppercase() } }
    }

    /**
     * 转为 kebab-case
     */
    fun toKebabCase(input: String): String {
        val normalized = normalizeToWords(input)
        return normalized.replace("_", "-")
    }

    /**
     * 标准化为单词
     */
    fun normalizeToWords(input: String): String {
        val snake = input.replace(Regex("([a-z])([A-Z])"), "$1_$2").replace("-", "_").replace(" ", "_")
        return snake.lowercase()
    }
}
