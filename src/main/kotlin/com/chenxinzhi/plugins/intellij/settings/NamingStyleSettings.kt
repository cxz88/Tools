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
    storages = [Storage("naming-style-settings.xml")]
)
class NamingStyleSettings : PersistentStateComponent<NamingStyleSettings> {
    // 无参构造函数，用于序列化
    constructor()

    // 用于深度克隆设置的方法
    fun createDeepCopy(): NamingStyleSettings {
        val copy = NamingStyleSettings()
        copy.namingStyles = this.namingStyles.map { style ->
            NamingStyle(
                style.name,
                style.order,
                style.useScript,
                style.scriptContent
            )
        }.toMutableList()
        return copy
    }

    // 存储命名风格及其顺序
    var namingStyles: MutableList<NamingStyle> = mutableListOf(
        NamingStyle(
            "camelCase", 0, true,
            """// camelCase
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
            "snake_case", 1, true,
            """// snake_case
import java.util.*

def parts = input.split("_")
def result = parts.collect { it.toLowerCase() }.join("_")
return result"""
        ),
        NamingStyle(
            "UPPER_SNAKE_CASE", 2, true,
            """// UPPER_SNAKE_CASE
import java.util.*

def parts = input.split("_")
def result = parts.collect { it.toUpperCase() }.join("_")
return result"""
        ),
        NamingStyle(
            "PascalCase", 3, true,
            """// PascalCase
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
        fun getInstance(): NamingStyleSettings {
            return com.intellij.openapi.application.ApplicationManager.getApplication()
                .getService(NamingStyleSettings::class.java)
        }
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

}
