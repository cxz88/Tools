package com.chenxinzhi.plugins.intellij.script

import com.intellij.openapi.diagnostic.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.script.ScriptContext
import javax.script.ScriptEngineManager
import javax.script.SimpleScriptContext

/**
 * Groovy脚本执行器
 * 
 * @author chenxinzhi
 * @date 2025-07-01
 */
class GroovyScriptEvaluator {
    private val logger = Logger.getInstance(GroovyScriptEvaluator::class.java)
    private val engine = ScriptEngineManager().getEngineByName("groovy")

    /**
     * 执行Groovy脚本
     * 
     * @param script 脚本内容
     * @param params 脚本参数
     * @return 脚本执行结果
     */
    suspend fun evaluate(script: String, params: Map<String, Any>): String? {
        if (engine == null) {
            logger.warn("Groovy脚本引擎未找到")
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val context = SimpleScriptContext()

                // 添加参数到脚本上下文
                params.forEach { (key, value) ->
                    context.setAttribute(key, value, ScriptContext.ENGINE_SCOPE)
                }

                // 执行脚本
                val result = engine.eval(script, context)
                result?.toString()
            } catch (e: Exception) {
                val scriptLines = script.lines().mapIndexed { i, line -> "${i+1}: $line" }.joinToString("\n")
                logger.error("Groovy脚本执行错误: ${e.message}\n脚本内容:\n$scriptLines\n参数: $params", e)
                null
            }
        }
    }
}
