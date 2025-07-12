package com.chenxinzhi.plugins.intellij.utils

import com.chenxinzhi.plugins.intellij.services.HandlerService
import com.chenxinzhi.plugins.intellij.services.InfluxQueryService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// 这是一个简化的示例。在真实插件中，你需要管理连接池、凭证等。
@Service(Service.Level.PROJECT)
class InfluxDBManager(private val project: Project) {

    // 假设你有一个方法可以获取当前活动的连接
    private fun getCurrentConnection(): InfluxDBConnection? {
        // TODO: 从你的 InfluxDBPanel 或其他地方获取当前连接信息
        // 例如: return InfluxDBPanel.getInstance(project).getActiveConnection()
        // 这里为了演示，我们返回一个模拟的连接
        return InfluxDBConnection("http://localhost:8086", "mydb")
    }

    // 模拟的连接信息
    data class InfluxDBConnection(val url: String, val database: String)

    var list1: List<String> = emptyList()
    var list2: Map<String, List<String>> = emptyMap()
    fun Project.load() {
        service<HandlerService>().handler {
            withContext(Dispatchers.IO) {
                list1 = run {
                    val sql = "SHOW MEASUREMENTS"
                    val query = InfluxQueryService.query(sql)
                    query.values.firstOrNull()?.flatMap { it.values } ?: emptyList() // 模拟数据
                }
                list2 = run {
                    list1.associateWith {
                        {
                            val sql = "SHOW FIELD KEYS FROM \"$it\""
                            val query = InfluxQueryService.query(sql)
                            val sql1 = "SHOW TAG KEYS FROM \"$it\""
                            val query1 = InfluxQueryService.query(sql1)
                            query.filter { it.value.firstOrNull()?.keys?.contains("fieldKey") == true }
                                .flatMap {
                                    it.value.firstOrNull()?.values ?: emptyList()
                                } + query1.flatMap { it.value.firstOrNull()?.values ?: emptyList() } // 模拟数据} }
                        }()
                    }
                }
            }
        }
    }

    // 从数据库获取所有表名
    fun fetchMeasurements(): List<String> {
        return list1
    }

    // 从数据库获取指定表的字段名
    fun fetchFieldKeys(measurement: String): List<String> {
        return list2[measurement] ?: emptyList()
    }
}