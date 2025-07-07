package com.chenxinzhi.plugins.intellij.utils

import com.chenxinzhi.plugins.intellij.services.InfluxQueryService
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

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

    // 从数据库获取所有表名
    fun fetchMeasurements(): List<String> {
        val sql = "SHOW MEASUREMENTS"
        val query = InfluxQueryService.query(sql)
        return query.values.firstOrNull()?.flatMap { it.values }?:emptyList() // 模拟数据
    }

    // 从数据库获取指定表的字段名
    fun fetchFieldKeys(measurement: String): List<String> {
        val sql = "SHOW FIELD KEYS FROM \"$measurement\""
        val query = InfluxQueryService.query(sql)
        val sql1 = "SHOW TAG KEYS FROM \"$measurement\""
        val query1 = InfluxQueryService.query(sql1)
        return query.filter { it.value.firstOrNull()?.keys?.contains("fieldKey")==true }
            .flatMap { it.value.firstOrNull()?.values?:emptyList() } + query1.flatMap { it.value.firstOrNull()?.values?:emptyList() } // 模拟数据
    }
}