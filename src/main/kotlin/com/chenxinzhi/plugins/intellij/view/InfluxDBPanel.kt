package com.chenxinzhi.plugins.intellij.view

import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.InfluxDbProjectSettingsService
import com.chenxinzhi.plugins.intellij.utils.onChange
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.sql.psi.SqlLanguage
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import org.codehaus.jettison.json.JSONObject
import java.awt.*
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.DefaultTableModel

class InfluxDBPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val influxUrlField = JTextField("")
    private val dbNameField = JTextField("")
    private val userField = JTextField("")
    private val passwordField = JPasswordField("")

    private val editorField = EditorTextFieldProvider.getInstance().getEditorField(
        InfluxQLLanguage,
        project,
        emptyList()
    ).apply {
        preferredSize = Dimension(100, 100)

    }

    private val resultTable = JBTable(object : DefaultTableModel() {
        override fun isCellEditable(row: Int, column: Int) = false
    }).apply {
        autoResizeMode = JBTable.AUTO_RESIZE_ALL_COLUMNS  // ✅ 让列宽自动平铺
        setRowSelectionAllowed(true)
        setFillsViewportHeight(true) // ✅ 可选：让表格撑满 viewport
    }

    private val totalCountLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.total", 0).get())
    private val pageInfoLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.pageInfo", 1, 1).get())

    private var currentPage = 0
    private val pageSize = 50
    private fun saveSettings() {
        val state = InfluxDbProjectSettingsService.getInstance(project).state
        state.influxUrl = influxUrlField.text
        state.dbName = dbNameField.text
        state.user = userField.text
        state.password = String(passwordField.password)
    }

    init {
        val state = InfluxDbProjectSettingsService.getInstance(project).state
        influxUrlField.text = state.influxUrl
        dbNameField.text = state.dbName
        userField.text = state.user
        passwordField.text = state.password

        influxUrlField.onChange { saveSettings() }
        dbNameField.onChange { saveSettings() }
        userField.onChange { saveSettings() }
        passwordField.onChange { saveSettings() }
        val runButton = JButton(LanguageBundle.messagePointer("tool.influxDb.query").get()).apply {
            preferredSize = Dimension(90, 28)
            margin = JBUI.insets(2, 8)
        }

        runButton.addActionListener {
            val sql = editorField.text.trim()
            if (sql.isNotEmpty()) {
                val paginatedSql = "$sql LIMIT $pageSize OFFSET ${currentPage * pageSize}"
                val result = InfluxQueryService.query(
                    influxUrlField.text.trim(),
                    dbNameField.text.trim(),
                    userField.text.trim(),
                    String(passwordField.password),
                    paginatedSql
                )
                resultTable.model = buildTableModel(result)

                val totalCount = InfluxQueryService.count(
                    influxUrlField.text.trim(),
                    dbNameField.text.trim(),
                    userField.text.trim(),
                    String(passwordField.password),
                    sql
                )

                totalCountLabel.text = LanguageBundle.messagePointer("tool.influxDb.total", totalCount).get()
                val totalPages = if (totalCount == 0) 1 else (totalCount + pageSize - 1) / pageSize
                pageInfoLabel.text =
                    LanguageBundle.messagePointer("tool.influxDb.pageInfo", currentPage + 1, totalPages).get()
            }
        }

        val configPanel = JPanel(GridBagLayout()).apply {
            border = BorderFactory.createTitledBorder(LanguageBundle.messagePointer("tool.influxDb.connectInfo").get())

            val insets = JBUI.insets(4, 8)

            fun addLabelAndField(gridx: Int, gridy: Int, label: String, field: JComponent) {
                add(JLabel(label), GridBagConstraints().apply {
                    this.gridx = gridx
                    this.gridy = gridy
                    this.anchor = GridBagConstraints.EAST
                    this.insets = insets
                })

                add(field, GridBagConstraints().apply {
                    this.gridx = gridx + 1
                    this.gridy = gridy
                    this.weightx = 1.0
                    this.fill = GridBagConstraints.HORIZONTAL
                    this.insets = insets
                })
            }

            addLabelAndField(0, 0, "URL:", influxUrlField)
            addLabelAndField(2, 0, LanguageBundle.messagePointer("tool.gen.select.database").get(), dbNameField)
            addLabelAndField(0, 1, LanguageBundle.messagePointer("tool.influxDb.user").get(), userField)
            addLabelAndField(2, 1, LanguageBundle.messagePointer("tool.influxDb.pwd").get(), passwordField)
        }


        val paginationPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(JButton(LanguageBundle.messagePointer("tool.influxDb.previousPage").get()).apply {
                addActionListener {
                    if (currentPage > 0) {
                        currentPage--
                        runButton.doClick()
                    }
                }
            })
            add(JButton(LanguageBundle.messagePointer("tool.influxDb.nextPage").get()).apply {
                addActionListener {
                    currentPage++
                    runButton.doClick()
                }
            })
            add(pageInfoLabel)
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

        val centerPanel = JPanel(BorderLayout(10, 10)).apply {
            add(topPanel, BorderLayout.NORTH)
            add(JBScrollPane(resultTable), BorderLayout.CENTER)
        }

        layout = BorderLayout(10, 10)
        add(configPanel, BorderLayout.NORTH)
        add(centerPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    private fun buildTableModel(data: List<Map<String, String>>): DefaultTableModel {
        if (data.isEmpty()) return DefaultTableModel()
        val columns = data[0].keys.toTypedArray()

        val rows = data.map { row ->
            columns.map { column ->
                val rawValue = row[column] ?: ""
                if (column.equals("time", ignoreCase = true)) {
                    formatTime(rawValue)
                } else {
                    rawValue
                }
            }.toTypedArray()
        }

        return object : DefaultTableModel(rows.toTypedArray(), columns) {
            override fun isCellEditable(row: Int, column: Int) = false
        }
    }

    private fun formatTime(utcTime: String): String {
        return try {
            val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS]'Z'")
                .withZone(ZoneOffset.UTC)

            val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Shanghai"))

            val instant = Instant.from(inputFormat.parse(utcTime))
            outputFormat.format(instant)
        } catch (e: Exception) {
            utcTime // 解析失败就原样返回
        }
    }

}

object InfluxQueryService {
    private val client = OkHttpClient()


    fun query(
        url: String,
        db: String,
        user: String,
        password: String,
        sql: String
    ): List<Map<String, String>> {
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

                if (results.length() == 0) return emptyList()

                val series = results.getJSONObject(0).optJSONArray("series")
                    ?: return emptyList()

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
        } catch (e: Exception) {
            Messages.showErrorDialog(
                "An error occurred during query:\n${e.message}",
                LanguageBundle.messagePointer("tool.influxDb.searchError").get()
            )
            // 根据需求决定返回空列表还是继续抛出异常
            return emptyList()
        }
    }


    fun count(url: String, db: String, user: String, password: String, originalSql: String): Int {
        try {
            val fromIndex = originalSql.indexOf("from", ignoreCase = true)
            if (fromIndex == -1) return 0
            val fromClause = originalSql.substring(fromIndex)
            val countSql = "SELECT COUNT(*) $fromClause"
            val result = query(url, db, user, password, countSql)
            val countValue = result.firstOrNull()?.values?.toList()?.get(1) ?: "0"
            return countValue.toIntOrNull() ?: 0
        } catch (e: Exception) {
            // 在IDEA弹窗提示错误
            Messages.showErrorDialog(
                "An error occurred during query:\n${e.message}",
                LanguageBundle.messagePointer("tool.influxDb.searchError").get()
            )
            // 根据需求决定返回空列表还是继续抛出异常
            return 0

        }
    }

}
