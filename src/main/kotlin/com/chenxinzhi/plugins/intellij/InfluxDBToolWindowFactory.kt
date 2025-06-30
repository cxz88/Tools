package com.chenxinzhi.plugins.intellij

import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import org.codehaus.jettison.json.JSONObject
import java.awt.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.swing.*
import javax.swing.table.DefaultTableModel
import java.net.HttpURLConnection
import java.net.URL


class InfluxDBPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val sqlLanguage = com.intellij.sql.psi.SqlLanguage.INSTANCE

    private val editorField = EditorTextFieldProvider.getInstance().getEditorField(
        sqlLanguage,
        project,
        emptyList()
    ).apply {
        preferredSize = Dimension(100, 100)
    }

    private val resultTable = JBTable(object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int): Boolean = false
    }).apply {
        autoResizeMode = JBTable.AUTO_RESIZE_OFF
        setRowSelectionAllowed(true)
    }

    private val totalCountLabel = JLabel("共 0 条")
    private var currentPage = 0
    private val pageSize = 50

    init {
        val runButton = JButton("执行查询")
        runButton.addActionListener {
            val sql = editorField.text.trim()
            if (sql.isNotEmpty()) {
                val paginatedSql = "$sql LIMIT $pageSize OFFSET ${currentPage * pageSize}"
                val result = InfluxQueryService.query(paginatedSql)
                resultTable.model = buildTableModel(result)
                totalCountLabel.text = "共 ${InfluxQueryService.count(sql)} 条"
            }
        }

        val paginationPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(JButton("上一页").apply {
                addActionListener {
                    if (currentPage > 0) {
                        currentPage--
                        runButton.doClick()
                    }
                }
            })
            add(JButton("下一页").apply {
                addActionListener {
                    currentPage++
                    runButton.doClick()
                }
            })
        }

        val topPanel = JPanel(BorderLayout(10, 10)).apply {
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
            add(editorField, BorderLayout.CENTER)
            add(runButton, BorderLayout.EAST)
        }

        val bottomPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 10, 5, 10)
            add(totalCountLabel, BorderLayout.WEST)
            add(paginationPanel, BorderLayout.EAST)
        }

        add(topPanel, BorderLayout.NORTH)
        add(JBScrollPane(resultTable), BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun buildTableModel(data: List<Map<String, String>>): DefaultTableModel {
        if (data.isEmpty()) return DefaultTableModel()
        val columns = data[0].keys.toTypedArray()
        val rows = data.map { row -> columns.map { row[it] ?: "" }.toTypedArray() }
        return object : DefaultTableModel(rows.toTypedArray(), columns) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
    }
}

object InfluxQueryService {
    private const val INFLUX_URL = "http://localhost:8086/query"
    private const val DB_NAME = "your_database"
    private const val USER = "user"
    private const val PASSWORD = "password"

    fun query(sql: String): List<Map<String, String>> {
        val encodedQuery = URLEncoder.encode(sql, StandardCharsets.UTF_8)
        val url = "$INFLUX_URL?db=$DB_NAME&q=$encodedQuery&u=$USER&p=$PASSWORD"

        val conn = URL(url).openConnection() as HttpURLConnection
        conn.requestMethod = "GET"

        val response = conn.inputStream.bufferedReader().readText()
        val json = JSONObject(response)
        val results = json.getJSONArray("results")
        if (results.length() == 0) return emptyList()

        val series = results.getJSONObject(0).optJSONArray("series") ?: return emptyList()
        if (series.length() == 0) return emptyList()

        val first = series.getJSONObject(0)
        val columns = first.getJSONArray("columns")
        val values = first.getJSONArray("values")

        return (0 until values.length()).map { i ->
            val row = values.getJSONArray(i)
            (0 until columns.length()).associate { j ->
                columns.getString(j) to row.optString(j)
            }
        }
    }

    fun count(originalSql: String): Int {
        val fromIndex = originalSql.indexOf("from", ignoreCase = true)
        if (fromIndex == -1) return 0
        val fromClause = originalSql.substring(fromIndex)
        val countSql = "SELECT COUNT(*) $fromClause"
        val result = query(countSql)
        val countValue = result.firstOrNull()?.values?.firstOrNull() ?: "0"
        return countValue.toIntOrNull() ?: 0
    }
}
