package com.chenxinzhi.plugins.intellij.influxdb.language.highlighter

import com.chenxinzhi.plugins.intellij.influxdb.language.highlighter.InfluxQLSyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import javax.swing.Icon

class InfluxQLColorSettingsPage : ColorSettingsPage {

    companion object {
        // 这个数组定义了在设置页面中用户可以自定义的颜色选项
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", InfluxQLSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Identifier", InfluxQLSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("String", InfluxQLSyntaxHighlighter.STRING),
            AttributesDescriptor("Number", InfluxQLSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Line Comment", InfluxQLSyntaxHighlighter.COMMENT),
            AttributesDescriptor("Operator", InfluxQLSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Parentheses", InfluxQLSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Bad Character", InfluxQLSyntaxHighlighter.BAD_CHARACTER)
        )
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = InfluxQLLanguage.id

    // 如果你有图标，可以在这里返回
    override fun getIcon(): Icon? = null

    override fun getHighlighter() = InfluxQLSyntaxHighlighter()

    // 这段代码是设置页面中的预览文本
    override fun getDemoText(): String {
        return """
            -- Show all measurements in the database
            SHOW MEASUREMENTS ON "mydb"

            -- Select data from the cpu measurement
            SELECT "usage_user", "usage_system"
            FROM "cpu_load"
            WHERE time > now() - 1h AND "host" = 'serverA'
            GROUP BY time(10m), "region" fill(none)
            
            -- This is an invalid character >#<
        """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, com.intellij.openapi.editor.colors.TextAttributesKey>? = null
}