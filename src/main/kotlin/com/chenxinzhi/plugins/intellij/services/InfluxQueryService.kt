package com.chenxinzhi.plugins.intellij.services

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.view.dbNameField
import com.chenxinzhi.plugins.intellij.view.influxUrlField
import com.chenxinzhi.plugins.intellij.view.passwordField
import com.chenxinzhi.plugins.intellij.view.userField
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.codehaus.jettison.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object InfluxQueryService {
    private val client = OkHttpClient()


    fun query(
        sql: String,
        url: String = influxUrlField.text,
        db: String = dbNameField.text,
        user: String = userField.text,
        password: String = String(passwordField.password),
    ): Map<String,List<Map<String, String>>> {
        try {
            val encodedQuery = URLEncoder.encode(sql, StandardCharsets.UTF_8)
            val fullUrl = "$url/query?db=$db&q=$encodedQuery"

            val request = Request.Builder()
                .url(fullUrl)
                .header("Authorization", Credentials.basic(user, password))
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw RuntimeException("Query failed: HTTP ${response.code} ${response.message}")
                }

                val responseBody = response.body.string()
                val json = JSONObject(responseBody)

                val results = json.optJSONArray("results")
                    ?: throw RuntimeException("Response format: Missing 'results' field")

                if (results.length() == 0) return emptyMap()

                val series = results.getJSONObject(0).optJSONArray("series")
                    ?: return emptyMap()

                if (series.length() == 0) return emptyMap()

                return (0 until series.length()).map { series.getJSONObject(it) }.associate { jSONObject ->
                    val columns = jSONObject.getJSONArray("columns")
                    val values = jSONObject.getJSONArray("values")

                    try {
                        jSONObject.getJSONObject("tags").toString()
                    } catch (_: Exception) {
                        ""
                    } to (0 until values.length()).map { i ->
                        val row = values.getJSONArray(i)
                        (0 until columns.length()).associate { j ->
                            columns.getString(j) to row.optString(j)
                        }
                    }

                }

            }
        } catch (e: Exception) {
            Messages.showErrorDialog(
                "An error occurred during query:\n${e.message}",
                LanguageBundle.messagePointer("tool.influxDb.searchError").get()
            )
            // 根据需求决定返回空列表还是继续抛出异常
            return emptyMap()
        }
    }


    fun count(url: String, db: String, user: String, password: String, originalSql: String): Int {
        try {
            val fromIndex = originalSql.indexOf("from", ignoreCase = true)
            if (fromIndex == -1) return 0
            val fromClause = originalSql.substring(fromIndex)
            val countSql = "SELECT COUNT(*) $fromClause"
            val result = query(countSql).values.toList()
            val countValue = result.firstOrNull()?.firstOrNull()?.values?.toList()?.get(1) ?: "0"
            return countValue.toIntOrNull() ?: 0
        } catch (e: Exception) {
            // 在IDEA弹窗提示错误
            Messages.showErrorDialog(
                "${e.message}",
                LanguageBundle.messagePointer("tool.influxDb.searchError").get()
            )
            // 根据需求决定返回空列表还是继续抛出异常
            return 0

        }
    }

}