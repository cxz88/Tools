package com.chenxinzhi.plugins.intellij.view

import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.InfluxDbProjectSettingsService
import com.chenxinzhi.plugins.intellij.services.InfluxQueryService
import com.chenxinzhi.plugins.intellij.utils.onChange
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextFieldProvider
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import kotlinx.coroutines.flow.MutableSharedFlow
import org.jetbrains.jewel.bridge.JewelComposePanel
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.SimpleTabContent
import org.jetbrains.jewel.ui.component.TabData
import org.jetbrains.jewel.ui.component.TabStrip
import org.jetbrains.jewel.ui.theme.defaultTabStyle
import java.awt.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.table.DefaultTableModel

// 这些字段保持在类外，与您原始代码一致
val influxUrlField = JTextField("192.168.11.211:8087")
val dbNameField = JTextField("admin")
val userField = JTextField("test")
val passwordField = JPasswordField("1qaz2wsx!@#$")

class InfluxDBPanel(private val project: Project) : JPanel(BorderLayout()) {

    private val editorField = EditorTextFieldProvider.getInstance().getEditorField(
        InfluxQLLanguage,
        project,
        emptyList()
    ).apply {
        preferredSize = Dimension(100, 100)
    }
    // ✅ 将单个JBTable替换为JBTabbedPane，用于容纳多个结果表格
    private var resultTabs: JComponent? = JewelComposePanel {
        JewelComposePanel {
            val tabDataDefaults = remember(tabs, tabsIndex) {
                tabs.mapIndexed { index, id ->
                    TabData.Default(
                        selected = index == tabsIndex,
                        content = { tabState ->
                            SimpleTabContent(label = id, state = tabState, icon = null)
                        },
                        onClick = { tabsIndex = index },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TabStrip(
                    tabs = tabDataDefaults,
                    style = JewelTheme.defaultTabStyle,
                    modifier = Modifier.weight(1f)
                )
            }
            if (components != null) {
                SwingPanel(factory = {
                    components[tabsIndex]
                })
            }


        }
    }

    private val totalCountLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.total", 0).get())
    private val pageInfoLabel = JLabel(LanguageBundle.messagePointer("tool.influxDb.pageInfo", 1, 1).get())

    private var currentPage = 0
    private val pageSize = 50

    private var tabs: List<String> by mutableStateOf(listOf(""))
    private var tabsIndex by mutableStateOf(0)

    var componentsJb: List<JBTable>? = null

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
                val result = InfluxQueryService.query(paginatedSql)
                // ✅ 清除旧的结果，并为每个数据系列（series）创建新的标签页
                if (result.isNotEmpty()) {
                    tabs = result.keys.toList()
                    tabsIndex = 0
                    componentsJb = result.map { (seriesName, data) ->
                        val tableModel = buildTableModelForSeries(data)
                        JBTable(tableModel).apply {
                            autoResizeMode = JBTable.AUTO_RESIZE_ALL_COLUMNS
                            setRowSelectionAllowed(true)
                            setFillsViewportHeight(true)
                        }
                    }

                } else {
                    // 可选：当没有结果时显示一条消息
                    val noResultsPanel = JPanel(GridBagLayout())
                    noResultsPanel.add(JLabel("查询未返回任何结果。"))
                }

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
            // ✅ 将选项卡面板添加到布局中，而不是原来的单个滚动面板
            add(resultTabs, BorderLayout.CENTER)

        }

        layout = BorderLayout(10, 10)
        add(configPanel, BorderLayout.NORTH)
        add(centerPanel, BorderLayout.CENTER)
        add(bottomPanel, BorderLayout.SOUTH)
    }

    /**
     * ✅ 重构后的方法，为单个数据系列列表构建TableModel
     * @param data 单个系列的数据列表
     * @return DefaultTableModel
     */
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
            // 兼容可选的纳秒精度 (e.g., .SSS, .SSSSSS, .SSSSSSSSS)
            val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSSSSS]'Z'")
                .withZone(ZoneOffset.UTC)
            // 使用系统默认时区进行显示，对用户更友好
            val outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())

            val instant = Instant.from(inputFormat.parse(utcTime))
            outputFormat.format(instant)
        } catch (e: Exception) {
            utcTime // 如果解析失败，返回原始字符串
        }
    }
}