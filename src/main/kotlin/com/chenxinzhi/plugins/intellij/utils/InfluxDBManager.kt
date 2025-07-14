package com.chenxinzhi.plugins.intellij.utils

import com.chenxinzhi.plugins.intellij.services.HandlerService
import com.chenxinzhi.plugins.intellij.services.InfluxQueryService
import com.chenxinzhi.plugins.intellij.view.InfluxDBPanel
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Service(Service.Level.PROJECT)
class InfluxDBManager(private val project: Project) {


    var list1: List<String> = emptyList()
    var list2: Map<String, List<String>> = emptyMap()
    fun InfluxDBPanel.load(block: () -> Unit) {
        project.service<HandlerService>().handler {
            withContext(Dispatchers.IO) {
                try {
                    block()
                    list1 = run {
                        val sql = "SHOW MEASUREMENTS"
                        val query = with(InfluxQueryService) {
                            query(sql)
                        }

                        query.values.firstOrNull()?.flatMap { it.values } ?: emptyList() // 模拟数据
                    }
                    list2 = run {
                        list1.associateWith {
                            {
                                val sql = "SHOW FIELD KEYS FROM \"$it\""
                                val query = with(InfluxQueryService) {
                                    query(sql)
                                }
                                val sql1 = "SHOW TAG KEYS FROM \"$it\""
                                val query1 = with(InfluxQueryService) {
                                    query(sql1)
                                }
                                query.filter { it.value.firstOrNull()?.keys?.contains("fieldKey") == true }
                                    .flatMap {
                                        it.value.firstOrNull()?.values ?: emptyList()
                                    } + query1.flatMap { it.value.firstOrNull()?.values ?: emptyList() } // 模拟数据} }
                            }()
                        }
                    }
                } catch (_: Exception) {

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