package com.chenxinzhi.plugins.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

/**
 * 翻译缓存服务，用于存储从Excel导入的手动翻译内容
 */
@Service(Service.Level.PROJECT)
class TranslationCacheService {

    // 存储中文到目标语言的翻译映射
    private val translationCache = mutableMapOf<String, Pair<String,Pair<String, String>>>()

    /**
     * 添加翻译映射
     */
    fun addTranslation(chinese: String, translation: Pair<String,Pair<String, String>>) {
        translationCache[chinese] = translation
    }

    /**
     * 批量添加翻译映射
     */
    fun addTranslations(translations: Map<String, Pair<String,Pair<String, String>>>) {
        translationCache.putAll(translations)
    }

    /**
     * 获取翻译
     */
    fun getTranslation(chinese: String): Pair<String,Pair<String, String>>? {
        return translationCache[chinese]
    }

    /**
     * 检查是否存在翻译
     */
    fun hasTranslation(chinese: String): Boolean {
        return translationCache.containsKey(chinese)
    }

    /**
     * 清空缓存
     */
    fun clear() {
        translationCache.clear()
    }



    /**
     * 获取缓存大小
     */
    fun size(): Int {
        return translationCache.size
    }

    companion object {
        fun getInstance(project: Project): TranslationCacheService = project.service()
    }
}
