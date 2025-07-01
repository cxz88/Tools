package com.chenxinzhi.plugins.intellij.script

import com.chenxinzhi.plugins.intellij.language.LanguageBundle

/**
 * Groovy脚本示例提供者
 * 
 * @author chenxinzhi
 * @date 2025-07-01
 */
object GroovyScriptExampleProvider {
    /**
     * 获取基本的命名风格转换脚本示例
     */
    fun getBasicExample(): String {
        // 根据当前语言选择不同的注释
        val commentText1 = LanguageBundle.message("script.comment.import")
        val commentText2 = LanguageBundle.message("script.comment.input")
        val commentText3 = LanguageBundle.message("script.comment.example")
        val commentText4 = LanguageBundle.message("script.comment.return")

        return """
            // $commentText1
            import java.util.*

            // $commentText2
            // $commentText3

            def parts = input.split("_")
            def result = ""
            parts.eachWithIndex { part, index -> 
                if (index == 0) {
                    result += part
                } else {
                    result += part.substring(0, 1).toUpperCase() + part.substring(1)
                }
            }

            // $commentText4
            return result
        """.trimIndent()
    }

}
