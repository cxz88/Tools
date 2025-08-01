package com.chenxinzhi.plugins.intellij.view

import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.InfluxDbProjectSettingsService
import com.chenxinzhi.plugins.intellij.services.InfluxQueryService
import com.chenxinzhi.plugins.intellij.utils.InfluxDBManager
import com.chenxinzhi.plugins.intellij.utils.onChange
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.DefaultTableModel


class InfluxDBPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val editorField = object : LanguageTextField(InfluxQLLanguage, project, "", false) {
        // 重写 createEditor 方法
        override fun createEditor(): EditorEx {
            // 首先，调用父类的方法来创建 editor 实例
            val editor = super.createEditor()
            editor.settings.isLineNumbersShown = true
            editor.settings.isIndentGuidesShown = true



            return editor
        }
    }.apply { preferredSize = Dimension(100, 100) }
    val influxUrlField = JTextField("")
    val dbNameField = JTextField("")
    val userField = JTextField("")
    val passwordField = JPasswordField("")

    @Suppress("unused")
    private val resultTabs = JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT)

    private val totalCountLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.total", 0).get())
    private val pageInfoLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.pageInfo", 1, 1).get())

    private var currentPage = 0
    private val pageSize = 50
    val dbManager: InfluxDBManager? = project.getService(InfluxDBManager::class.java)
    private fun saveSettings() {
        dbManager?.let {
            with(it) {
                load {
                    val state = InfluxDbProjectSettingsService.getInstance(project).state
                    state.influxUrl = influxUrlField.text
                    state.dbName = dbNameField.text
                    state.user = userField.text
                    state.p = String(passwordField.password)
                }
            }

        }
    }

    init {
        val state = InfluxDbProjectSettingsService.getInstance(project).state
        invokeLater {
            influxUrlField.text = state.influxUrl
            dbNameField.text = state.dbName
            userField.text = state.user
            passwordField.text = state.p

            influxUrlField.onChange { saveSettings() }
            dbNameField.onChange { saveSettings() }
            userField.onChange { saveSettings() }
            passwordField.onChange { saveSettings() }
        }

        val runButton = JButton(LanguageBundle.messagePointer("tool.influxDb.query").get()).apply {
            preferredSize = Dimension(90, 28)
            margin = JBUI.insets(2, 8)
        }

        runButton.addActionListener {
            val sql = editorField.text.trim()
            if (sql.isNotEmpty()) {
                val paginatedSql = "$sql LIMIT $pageSize OFFSET ${currentPage * pageSize}"
                val result = with(InfluxQueryService) {
                    query(paginatedSql)
                }

                // ✅ 清除旧的结果，并为每个数据系列（series）创建新的标签页
                resultTabs.removeAll()
                if (result.isNotEmpty()) {
                    result.forEach { (seriesName, data) ->
                        val tableModel = buildTableModelForSeries(data)
                        val table = JBTable(tableModel).apply {
                            autoResizeMode = JBTable.AUTO_RESIZE_ALL_COLUMNS
                            setRowSelectionAllowed(true)
                            setFillsViewportHeight(true)
                        }
                        // 使用seriesName作为标签页标题，并将包含表格的滚动面板作为内容
                        resultTabs.addTab(seriesName, JBScrollPane(table))
                    }
                } else {
                    // 可选：当没有结果时显示一条消息
                    val noResultsPanel = JPanel(GridBagLayout())
                    noResultsPanel.add(JLabel(LanguageBundle.messagePointer("tool.influxDb.noInformationFound").get()))
                    resultTabs.addTab(LanguageBundle.messagePointer("tool.influxDb.result").get(), noResultsPanel)
                }


                val totalCount = with(InfluxQueryService) {
                    count(
                        sql
                    )
                }

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
            add(resultTabs, BorderLayout.CENTER)
        }

        layout = BorderLayout(10, 10)
        add(configPanel, BorderLayout.NORTH)
        add(centerPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }


    private fun buildTableModelForSeries(data: List<Map<String, String>>): DefaultTableModel {
        if (data.isEmpty()) return DefaultTableModel()

        val columns = data.firstOrNull()?.keys?.toTypedArray() ?: emptyArray()

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
            val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]'Z'")
                .withZone(ZoneOffset.UTC)
            val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())

            val instant = Instant.from(inputFormat.parse(utcTime))
            outputFormat.format(instant)
        } catch (_: Exception) {
            utcTime
        }
    }
}