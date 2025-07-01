package com.chenxinzhi.plugins.intellij.settings

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * 命名风格设置类，用于持久化存储命名风格配置
 *
 * @author chenxinzhi
 * @date 2025-07-01
 */
@Service(Service.Level.APP)
@State(
    name = "com.chenxinzhi.plugins.intellij.settings.NamingStyleSettings",
    storages = [Storage("NamingStyleSettings.xml")]
)
class NamingStyleSettings : PersistentStateComponent<NamingStyleSettings> {

    // 存储命名风格及其顺序
    var namingStyles: MutableList<NamingStyle> = mutableListOf(
        NamingStyle(
            "camelCase", 0, "toCamelCase", true,
            """// 驼峰式命名 (camelCase)
import java.util.*

def parts = input.split('_')
def result = new StringBuilder()
parts.eachWithIndex { part, index ->
    if (index == 0) {
        result.append(part.toLowerCase())
    } else {
        result.append(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
    }
}
return result.toString()"""
        ),
        NamingStyle(
            "snake_case", 1, "toSnakeCase", true,
            """// 蛇形命名 (snake_case)
    import java.util.*

    def parts = input.split("_")
    def result = parts.collect { it.toLowerCase() }.join("_")
    return result"""
        ),
        NamingStyle(
            "UPPER_SNAKE_CASE", 2, "toUpperSnakeCase", true,
            """// 大写蛇形命名 (UPPER_SNAKE_CASE)
    import java.util.*

    def parts = input.split("_")
    def result = parts.collect { it.toUpperCase() }.join("_")
    return result"""
        ),
        NamingStyle(
            "PascalCase", 3, "toPascalCase", true,
            """// 帕斯卡命名 (PascalCase)
    import java.util.*

    def parts = input.split("_")
    def result = parts.collect { part -> 
    part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()
    }.join('')
    return result"""
        )
    )

    override fun getState(): NamingStyleSettings = this

    override fun loadState(state: NamingStyleSettings) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        @JvmStatic
        fun getInstance(): NamingStyleSettings = service()
    }

    /**
     * 获取排序后的命名风格列表
     */
    fun getSortedStyles(): List<NamingStyle> {
        return namingStyles.sortedBy { it.order }
    }

    /**
     * 移动命名风格顺序
     */
    fun moveStyle(index: Int, direction: Int): Boolean {
        if (index < 0 || index >= namingStyles.size) return false
        val newIndex = index + direction
        if (newIndex < 0 || newIndex >= namingStyles.size) return false

        // 交换order值
        val temp = namingStyles[index].order
        namingStyles[index].order = namingStyles[newIndex].order
        namingStyles[newIndex].order = temp

        return true
    }

    /**
     * 添加新的命名风格
     */
    fun addNamingStyle(name: String, methodName: String, useScript: Boolean, scriptContent: String = ""): Boolean {
        // 获取最大的order值并加1
        val maxOrder = namingStyles.maxOfOrNull { it.order } ?: -1
        val style = NamingStyle(name, maxOrder + 1, methodName, useScript, scriptContent)
        return namingStyles.add(style)
    }

    /**
     * 删除命名风格，但至少保留一个
     */
    fun removeNamingStyle(index: Int): Boolean {
        if (namingStyles.size <= 1) return false
        if (index < 0 || index >= namingStyles.size) return false

        namingStyles.removeAt(index)
        // 重新排序
        namingStyles.forEachIndexed { i, style -> style.order = i }
        return true
    }

    /**
     * 更新命名风格
     */
    fun updateNamingStyle(
        index: Int,
        name: String,
        methodName: String,
        useScript: Boolean,
        scriptContent: String = ""
    ): Boolean {
        if (index < 0 || index >= namingStyles.size) return false

        val style = namingStyles[index]
        style.name = name
        style.methodName = methodName
        style.useScript = useScript
        style.scriptContent = scriptContent
        return true
    }
}

/**
 * 命名风格数据类
 */
data class NamingStyle(
    var name: String, // 显示名称
    var order: Int,   // 顺序
    var methodName: String, // 对应的方法名
    var useScript: Boolean, // 是否使用脚本
    var scriptContent: String = "" // 脚本内容
)
