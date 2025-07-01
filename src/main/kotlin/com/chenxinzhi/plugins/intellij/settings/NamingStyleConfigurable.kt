package com.chenxinzhi.plugins.intellij.settings

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.COLUMNS_MEDIUM
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import javax.swing.*

/**
 * 命名风格配置界面
 *
 * @author chenxinzhi
 * @date 2025-07-01
 */
class NamingStyleConfigurable : Configurable, com.intellij.openapi.Disposable {
    private var project: com.intellij.openapi.project.Project? = null
    private val settings by lazy { NamingStyleSettings.getInstance() }
    private var modified = false
    private lateinit var styleListModel: DefaultListModel<NamingStyle>
    private lateinit var styleList: JBList<NamingStyle>

    // 保存已应用设置的深度拷贝，用于取消时恢复
    private var settingsSnapshot: NamingStyleSettings? = null

    // 原始脚本内容的备份 (key: 样式名称, value: 脚本内容)
    private val originalScriptContents = mutableMapOf<String, String>()

    // 是否已经应用了变更，用于追踪取消按钮点击
    private var appliedChanges = false

    private lateinit var nameField: JBTextField
    private lateinit var useScriptCheckBox: JBCheckBox
    private lateinit var scriptTextArea: JTextArea

    // 文档监听器
    private val nameDocumentListener = object : javax.swing.event.DocumentListener {
        override fun insertUpdate(e: javax.swing.event.DocumentEvent) {
            updateModelFromUI()
        }

        override fun removeUpdate(e: javax.swing.event.DocumentEvent) {
            updateModelFromUI()
        }

        override fun changedUpdate(e: javax.swing.event.DocumentEvent) {
            updateModelFromUI()
        }
    }

    private val scriptDocumentListener = object : com.intellij.openapi.editor.event.DocumentListener {
        override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
            updateModelFromUI()
        }
    }

    // 编辑器相关组件
    private lateinit var editor: com.intellij.openapi.editor.Editor
    private lateinit var editorDocument: com.intellij.openapi.editor.Document
    private var currentProject: com.intellij.openapi.project.Project? = null

    override fun getDisplayName(): String = LanguageBundle.message("settings.naming.style.title")

    override fun createComponent(): JComponent {
        // 获取当前项目
        project = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull()
            ?: com.intellij.openapi.project.ProjectManager.getInstance().defaultProject

        // 创建风格列表
        styleListModel = DefaultListModel()

        // 加载当前设置的样式到UI
        settings.getSortedStyles().forEach {
            styleListModel.addElement(it)
        }

        styleList = JBList(styleListModel)
        styleList.cellRenderer = NamingStyleListCellRenderer()
        styleList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        // 创建主面板
        val mainPanel = JPanel(BorderLayout())

        // 创建左侧列表面板
        val listPanel = panel {
            row {
                cell(JBScrollPane(styleList).apply {
                    preferredSize = Dimension(200, 300)
                })
            }
            row {
                button(LanguageBundle.message("settings.naming.style.add")) { addStyle() }
                button(LanguageBundle.message("settings.naming.style.remove")) { removeStyle() }
            }
            row {
                button(LanguageBundle.message("settings.naming.style.moveUp")) { moveStyleUp() }
                button(LanguageBundle.message("settings.naming.style.moveDown")) { moveStyleDown() }
            }
        }

        // 创建右侧编辑面板
        val formPanel = JPanel()
        formPanel.layout = BoxLayout(formPanel, BoxLayout.Y_AXIS)

        // 添加基本字段
        val basicFieldsPanel = panel {
            row(LanguageBundle.message("settings.naming.style.name")) {
                nameField = textField()
                    .columns(COLUMNS_MEDIUM)
                    .bindText({ "" }, { s -> /* 文本绑定回调不再需要 */ })
                    .component

                // 添加文档监听器
                nameField.document.addDocumentListener(nameDocumentListener)
            }
        }
        formPanel.add(basicFieldsPanel)

        // 我们默认总是使用脚本，不需要复选框
        useScriptCheckBox = JBCheckBox("")
        useScriptCheckBox.isSelected = true
        useScriptCheckBox.isVisible = false

        // 创建脚本编辑面板 - 使用简化的组件，更好的兼容性
        val scriptPanel = JPanel(BorderLayout())
        scriptPanel.preferredSize = Dimension(800, 400)  // 设置固定的首选大小
        scriptPanel.minimumSize = Dimension(600, 350)   // 设置最小大小

        val titleLabel = JLabel(LanguageBundle.message("settings.naming.style.script"))
        titleLabel.border = JBUI.Borders.empty(5)
        titleLabel.font = titleLabel.font.deriveFont(java.awt.Font.BOLD)
        scriptPanel.add(titleLabel, BorderLayout.NORTH)

        // 创建文档 - 使用本土化的脚本模板
        val editorFactory = com.intellij.openapi.editor.EditorFactory.getInstance()
        val scriptTemplate = """// ${LanguageBundle.message("script.comment.input")}

        def parts = input.split("_")
        def result = ""
        parts.eachWithIndex { part, index -> 
            if (index == 0) {
        result += part.toLowerCase()
            } else {
        result += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()
            }
        }
        // ${LanguageBundle.message("script.comment.return")}
        return result"""
        editorDocument = editorFactory.createDocument(scriptTemplate)

        // 监听文档变化
        editorDocument.addDocumentListener(scriptDocumentListener)

        // 获取当前项目 - 用于编辑器创建
        currentProject = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull()
            ?: com.intellij.openapi.project.ProjectManager.getInstance().defaultProject

        // 创建简单的编辑器
        try {
            // 创建具有语法高亮的编辑器
            val groovyFileType =
                com.intellij.openapi.fileTypes.FileTypeRegistry.getInstance().findFileTypeByName("Groovy")
                    ?: com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE

            editor = editorFactory.createEditor(editorDocument, currentProject, groovyFileType, false)

            // 基本设置
            val settings = editor.settings
            settings.isLineNumbersShown = true
            settings.isLineMarkerAreaShown = true
            settings.isFoldingOutlineShown = false  // 禁用代码折叠，避免高度变化
            settings.isSmartHome = true
            settings.additionalLinesCount = 0      // 减少额外的行数
            settings.additionalColumnsCount = 0    // 减少额外的列数
            settings.isUseSoftWraps = false        // 禁用软换行

            // 禁用不必要的功能，减少界面元素
            settings.isShowIntentionBulb = false
            settings.isAutoCodeFoldingEnabled = false

            // 创建固定高度的编辑器容器
            val editorContainer = JPanel(BorderLayout())
            editorContainer.preferredSize = Dimension(800, 300)  // 设置固定的编辑器高度
            editorContainer.add(editor.component, BorderLayout.CENTER)

            // 添加编辑器容器到脚本面板
            scriptPanel.add(editorContainer, BorderLayout.CENTER)
        } catch (_: Exception) {
            // 创建一个简单的文本区域
            val simpleTextArea = JTextArea(editorDocument.text)
            simpleTextArea.font = JBUI.Fonts.create("Monospaced", 12)
            simpleTextArea.document.addDocumentListener(nameDocumentListener)

            // 将文本区域添加到滚动面板
            scriptPanel.add(JScrollPane(simpleTextArea), BorderLayout.CENTER)

            // 创建一个代理文本区域，使用实际的编辑控件
            scriptTextArea = simpleTextArea
        }

        // 如果还没有创建代理文本区域（在使用编辑器时）
        if (!::scriptTextArea.isInitialized) {
            scriptTextArea = createProxyTextArea(editorDocument)
        }

        // 添加提示信息
        val commentLabel = JLabel(LanguageBundle.message("settings.naming.style.script.comment"))
        commentLabel.border = JBUI.Borders.empty(5)
        scriptPanel.add(commentLabel, BorderLayout.SOUTH)
        formPanel.add(scriptPanel)

        val editPanel = JPanel(BorderLayout())
        editPanel.add(formPanel, BorderLayout.CENTER)

        // 创建当前设置的快照
        settingsSnapshot = settings.createDeepCopy()

        // 布局 - 使用IntelliJ平台的分割面板组件
        val splitPane = com.intellij.ui.OnePixelSplitter(false, "NamingStyleConfigurable.splitter", 0.25f)
        splitPane.firstComponent = listPanel
        splitPane.secondComponent = editPanel
        mainPanel.add(splitPane, BorderLayout.CENTER)

        // 添加选择监听
        styleList.addListSelectionListener { e ->
            if (!e.valueIsAdjusting) {
                updateEditPanel()
            }
        }

        // 尝试添加窗口关闭监听器
        try {
            // 获取对话框窗口
            SwingUtilities.invokeLater {
                val window = SwingUtilities.getWindowAncestor(mainPanel)
                if (window is java.awt.Window) {
                    // 添加窗口监听器
                    window.addWindowListener(object : java.awt.event.WindowAdapter() {
                        override fun windowClosing(e: java.awt.event.WindowEvent) {
                            // 如果有修改但没有应用，恢复所有设置
                            if (modified && !appliedChanges) {
                                restoreAllSettings()
                            }
                        }
                    })
                }
            }
        } catch (_: Exception) {

        }

        // 添加取消按钮监听器，用于恢复脚本内容
        try {
            val parent = SwingUtilities.getWindowAncestor(mainPanel)
            if (parent is JDialog) {
                val rootPane = parent.rootPane
                val cancelButton = rootPane.getClientProperty("CANCEL_BUTTON")
                if (cancelButton is JButton) {
                    cancelButton.addActionListener {
                        // 当用户点击取消按钮时，恢复设置
                        if (settingsSnapshot != null) {
                            // 清空当前设置
                            settings.namingStyles.clear()

                            // 从快照复制所有样式
                            settingsSnapshot?.namingStyles?.forEach { original ->
                                val styleCopy = NamingStyle(
                                    original.name,
                                    original.order,
                                    original.useScript,
                                    original.scriptContent
                                )
                                settings.namingStyles.add(styleCopy)
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {

        }

        return mainPanel
    }

    private fun updateEditPanel() {
        val index = styleList.selectedIndex
        if (index >= 0 && index < settings.namingStyles.size) {
            val style = settings.namingStyles[index]

            // 移除可能存在的文档监听器，避免循环触发更新
            if (::nameField.isInitialized) {
                // 直接移除已知的文档监听器
                nameField.document.removeDocumentListener(nameDocumentListener)
            }

            nameField.text = style.name
            // 确保脚本总是启用
            useScriptCheckBox.isSelected = true

            // 如果脚本内容为空，则设置默认脚本
            if (style.scriptContent.isBlank()) {
                val defaultScript = com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getBasicExample()
                // 在UI更新前关闭监听器
                if (::editorDocument.isInitialized) {
                    editorDocument.removeDocumentListener(scriptDocumentListener)
                }
                com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
                    editorDocument.setText(defaultScript)
                }
                style.scriptContent = defaultScript
                // 重新添加文档监听器
                editorDocument.addDocumentListener(scriptDocumentListener)
            } else {
                // 在UI更新前关闭监听器
                if (::editorDocument.isInitialized) {
                    editorDocument.removeDocumentListener(scriptDocumentListener)
                }
                // 设置编辑器文本
                com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
                    editorDocument.setText(style.scriptContent)
                }
                // 重新添加文档监听器
                editorDocument.addDocumentListener(scriptDocumentListener)
            }

            // 重置编辑器滚动位置
            if (::editor.isInitialized) {
                editor.scrollingModel.scrollVertically(0)
            }

            scriptTextArea.isEnabled = true

            // 重新添加文档监听器
            nameField.document.addDocumentListener(nameDocumentListener)
        } else {
            nameField.text = ""
            useScriptCheckBox.isSelected = true
            scriptTextArea.text = ""
            scriptTextArea.isEnabled = true
        }
    }


    /**
     * 创建代理文本区域
     */
    private fun createProxyTextArea(document: com.intellij.openapi.editor.Document): JTextArea {
        return object : JTextArea() {
            override fun getText(): String {
                return document.text
            }

            override fun setText(t: String) {
                com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
                    document.setText(t)
                }
            }
        }
    }

    private fun addStyle() {
        val newStyle = NamingStyle(
            LanguageBundle.message("settings.naming.style.new"),
            styleListModel.size(),
            true, // 总是使用脚本
            com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getBasicExample() // 默认脚本内容
        )
        settings.namingStyles.add(newStyle)
        styleListModel.addElement(newStyle)
        styleList.selectedIndex = styleListModel.size() - 1
        modified = true
    }

    private fun removeStyle() {
        val index = styleList.selectedIndex
        if (index >= 0 && styleListModel.size() > 1) {
            settings.removeNamingStyle(index)
            styleListModel.remove(index)
            styleList.selectedIndex = minOf(index, styleListModel.size() - 1)
            modified = true
        }
    }

    private fun moveStyleUp() {
        // 在移动前先保存快照（如果尚未保存）
        if (settingsSnapshot == null) {
            settingsSnapshot = settings.createDeepCopy()
        }

        val index = styleList.selectedIndex
        if (index > 0) {
            if (settings.moveStyle(index, -1)) {
                // 更新列表显示
                val style = styleListModel.remove(index)
                styleListModel.add(index - 1, style)
                styleList.selectedIndex = index - 1
                modified = true
            }
        }
    }

    private fun moveStyleDown() {
        // 在移动前先保存快照（如果尚未保存）
        if (settingsSnapshot == null) {
            settingsSnapshot = settings.createDeepCopy()
        }

        val index = styleList.selectedIndex
        if (index >= 0 && index < styleListModel.size() - 1) {
            if (settings.moveStyle(index, 1)) {
                // 更新列表显示
                val style = styleListModel.remove(index)
                styleListModel.add(index + 1, style)
                styleList.selectedIndex = index + 1
                modified = true
            }
        }
    }

    /**
     * 从UI更新数据模型，但不保存设置
     */
    private fun updateModelFromUI() {
        // 在删除前先保存快照（如果尚未保存）
        if (settingsSnapshot == null) {
            settingsSnapshot = settings.createDeepCopy()
        }

        val index = styleList.selectedIndex
        if (index >= 0) {
            val name = nameField.text.trim()
            val script = scriptTextArea.text

            if (name.isNotEmpty()) {
                // 在第一次修改时保存快照（如果尚未保存）
                if (!modified && settingsSnapshot == null) {
                    settingsSnapshot = settings.createDeepCopy()
                }

                // 仅更新内存中的模型，不保存设置
                val style = settings.namingStyles[index]

                // 保存原始名称和脚本内容（如果尚未保存）
                val originalName = style.name
                if (!originalScriptContents.containsKey(originalName)) {
                    originalScriptContents[originalName] = style.scriptContent
                }

                // 更新样式
                style.name = name
                style.useScript = true
                style.scriptContent = script

                // 如果名称变更了，更新备份映射的键
                if (originalName != name && originalScriptContents.containsKey(originalName)) {
                    val content = originalScriptContents.remove(originalName)
                    if (content != null) {
                        originalScriptContents[name] = content
                    }
                }

                // 更新列表显示
                styleListModel.setElementAt(style, index)
                modified = true
            }
        }
    }

    /**
     * 处理取消按钮点击事件
     * 注意：此方法需要通过反射或其他方式在对话框关闭前调用
     */
    private fun handleCancel() {
        // 恢复所有脚本内容到原始状态
        if (originalScriptContents.isNotEmpty()) {
            for (style in settings.namingStyles) {
                originalScriptContents[style.name]?.let { originalContent ->
                    style.scriptContent = originalContent
                }
            }
        }
    }



    override fun isModified(): Boolean = modified

    override fun apply() {
        // 保存设置
        // 在这里不需要额外操作，因为我们已经在UI中实时更新了模型
        // 当用户点击应用或确定按钮时，IntelliJ会自动保存PersistentStateComponent

        // 更新设置快照
        settingHandler()

        modified = false
        appliedChanges = true
    }

    private fun settingHandler() {
        settingsSnapshot = settings.createDeepCopy()

        // 更新原始脚本内容备份
        originalScriptContents.clear()
        settings.namingStyles.forEach { style ->
            originalScriptContents[style.name] = style.scriptContent
        }

        // 更新原始脚本内容备份
        originalScriptContents.clear()
        settings.namingStyles.forEach { style ->
            originalScriptContents[style.name] = style.scriptContent
        }
    }

    override fun reset() {
        // 在设置面板每次打开时调用，加载当前的持久化设置

        // 保存当前设置状态的快照（用于后续比较修改状态）
        settingHandler()

        // 更新UI以反映当前的设置
        styleListModel.clear()
        settings.getSortedStyles().forEach { styleListModel.addElement(it) }

        // 如果列表不为空，选择第一个项
        if (styleListModel.size() > 0) {
            styleList.selectedIndex = 0
            updateEditPanel()
        }

        modified = false
        appliedChanges = false
    }

    override fun disposeUIResources() {
        // 如果用户没有点击应用，但做了修改，需要恢复所有设置
        if (modified && !appliedChanges) {
            restoreAllSettings()
        }
        // 如果用户没有点击应用，则在这里也尝试恢复原始脚本内容
        // 这是一个备选方案，以防CancelableUI接口方法不工作
        if (modified) {
            handleCancel()
        }
        com.intellij.openapi.util.Disposer.dispose(this)
    }

    /**
     * 完全恢复设置到最后应用的状态
     * 包括恢复所有添加、删除和修改的样式
     */
    private fun restoreAllSettings() {
        if (settingsSnapshot == null) return

        // 清空当前设置
        settings.namingStyles.clear()

        // 从快照恢复所有样式
        settingsSnapshot?.namingStyles?.forEach { original ->
            val styleCopy = NamingStyle(
                original.name,
                original.order,
                original.useScript,
                original.scriptContent
            )
            settings.namingStyles.add(styleCopy)
        }

        // 更新UI
        styleListModel.clear()
        settings.getSortedStyles().forEach { styleListModel.addElement(it) }

        // 恢复选择状态
        if (styleListModel.size() > 0) {
            styleList.selectedIndex = 0
            updateEditPanel()
        }

    }

    override fun dispose() {
        // 释放所有编辑器资源
        if (::editor.isInitialized) {
            com.intellij.openapi.editor.EditorFactory.getInstance().releaseEditor(editor)
        }
    }

    /**
     * 实现CancelableUI接口方法
     * 当用户点击取消按钮时会调用此方法
     */
    override fun cancel() {
        handleCancel()
    }

    /**
     * 命名风格列表渲染器
     */
    private class NamingStyleListCellRenderer : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(
            list: JList<*>?,
            value: Any?,
            index: Int,
            isSelected: Boolean,
            cellHasFocus: Boolean
        ): Component {
            val component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            if (value is NamingStyle) {
                text = value.name
                border = JBUI.Borders.empty(4)
            }
            return component
        }
    }
}
