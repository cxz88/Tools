package com.chenxinzhi.plugins.intellij.script

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
        return """
            // 必要的导入
            import java.util.*

            // 输入是已经标准化的单词 (snake_case 格式)
            // 例如: "hello_world"
            // 你需要返回转换后的文本

            def parts = input.split("_")
            def result = ""
            parts.eachWithIndex { part, index -> 
                if (index == 0) {
                    result += part
                } else {
                    result += part.substring(0, 1).toUpperCase() + part.substring(1)
                }
            }

            // 返回 camelCase 格式
            // 例如: "helloWorld"
            return result
        """.trimIndent()
    }

    /**
     * 获取高级的脚本示例，包含自定义逻辑
     */
    fun getAdvancedExample(): String {
        return """
            // 必要的导入
            import java.util.*
            import java.text.*

            // 自定义命名风格: kebab-case
            // 输入是已经标准化的单词 (snake_case 格式)

            // 转换为 kebab-case
            def result = input.replace("_", "-")

            // 返回 kebab-case 格式
            // 例如: "hello-world"
            return result
        """.trimIndent()
    }

    /**
     * 获取驼峰式转换脚本示例
     */
    fun getCamelCaseExample(): String {
        return """
            // 驼峰式命名 (camelCase)
            import java.util.*

            def parts = input.split("_")
            def result = new StringBuilder()
            parts.eachWithIndex { part, index -> 
                if (index == 0) {
                    result.append(part.toLowerCase())
                } else {
                    result.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                }
            }
            return result.toString()
        """.trimIndent()
    }

    /**
     * 获取帕斯卡式转换脚本示例
     */
    fun getPascalCaseExample(): String {
        return """
            // 帕斯卡式命名 (PascalCase)
            import java.util.*

            def parts = input.split("_")
            def result = parts.collect { part -> 
                part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()
            }.join('')
            return result
        """.trimIndent()
    }
}
