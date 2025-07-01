package com.chenxinzhi.plugins.intellij.settings

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

/**
 * 命名风格配置界面
 * 
 * @author chenxinzhi
 * @date 2025-07-01
 */
class NamingStyleConfigurable : Configurable, com.intellij.openapi.Disposable {
    private val settings = NamingStyleSettings.getInstance()
    private var modified = false
    private lateinit var panel: DialogPanel
    private lateinit var styleListModel: DefaultListModel<NamingStyle>
    private lateinit var styleList: JBList<NamingStyle>

    private lateinit var nameField: JBTextField
    private lateinit var methodField: JBTextField
    private lateinit var useScriptCheckBox: JBCheckBox
    private lateinit var scriptTextArea: JTextArea

    // 编辑器相关组件
    private lateinit var editor: com.intellij.openapi.editor.Editor
    private lateinit var editorDocument: com.intellij.openapi.editor.Document
    private lateinit var groovyPsiFile: com.intellij.psi.PsiFile
    private var currentProject: com.intellij.openapi.project.Project? = null

    override fun getDisplayName(): String = LanguageBundle.message("settings.naming.style.title")

    override fun createComponent(): JComponent {
        // 创建风格列表
        styleListModel = DefaultListModel()
        settings.getSortedStyles().forEach { styleListModel.addElement(it) }
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
                    .bindText({ "" }, { s -> modified = true })
                    .component
            }
            row(LanguageBundle.message("settings.naming.style.method")) {
                methodField = textField()
                    .columns(COLUMNS_MEDIUM)
                    .bindText({ "" }, { s -> modified = true })
                    .component
            }
        }
        formPanel.add(basicFieldsPanel)

        // 我们默认总是使用脚本，不需要复选框
        useScriptCheckBox = JBCheckBox("")
        useScriptCheckBox.isSelected = true
        useScriptCheckBox.isVisible = false

        // 创建脚本编辑面板 - 使用简化的组件，更好的兼容性
        val scriptPanel = JPanel(BorderLayout())
        val titleLabel = JLabel(LanguageBundle.message("settings.naming.style.script"))
        titleLabel.border = com.intellij.util.ui.JBUI.Borders.empty(5, 5, 5, 5)
        titleLabel.font = titleLabel.font.deriveFont(java.awt.Font.BOLD)
        scriptPanel.add(titleLabel, BorderLayout.NORTH)

        // 创建文档
        val editorFactory = com.intellij.openapi.editor.EditorFactory.getInstance()
        editorDocument = editorFactory.createDocument("""// Groovy脚本 - 用于命名风格转换
        // 可用变量:
        // - input: String - 输入字符串 (例如: user_name)

        import java.util.*

        // 定义输入变量
        def input = "sample_variable_name"

        // 您的命名转换逻辑:
        // 示例：将snake_case转换为camelCase
        def parts = input.split("_")
        def result = ""
        parts.eachWithIndex { part, index -> 
            if (index == 0) {
                result += part.toLowerCase()
            } else {
                result += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()
            }
        }
        return result
        """)

        // 监听文档变化
        editorDocument.addDocumentListener(object : com.intellij.openapi.editor.event.DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                modified = true
            }
        })

        // 获取当前项目 - 用于编辑器创建
        currentProject = com.intellij.openapi.project.ProjectManager.getInstance().openProjects.firstOrNull() 
                      ?: com.intellij.openapi.project.ProjectManager.getInstance().defaultProject

        // 创建简单的编辑器
        try {
            // 创建具有语法高亮的编辑器
            val groovyFileType = com.intellij.openapi.fileTypes.FileTypeRegistry.getInstance().findFileTypeByName("Groovy")
                          ?: com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE

            editor = editorFactory.createEditor(editorDocument, currentProject, groovyFileType, false)

            // 基本设置
            val settings = editor.settings
            settings.isLineNumbersShown = true
            settings.isLineMarkerAreaShown = true
            settings.isFoldingOutlineShown = true
            settings.isSmartHome = true
            settings.isAutoCodeFoldingEnabled = true

            // 添加编辑器到面板
            scriptPanel.add(editor.component, BorderLayout.CENTER)
        } catch (e: Exception) {
            // 如果创建编辑器失败，回退到简单的文本区域
            com.intellij.openapi.diagnostic.Logger.getInstance(NamingStyleConfigurable::class.java)
                .warn("创建编辑器失败，使用简单文本区域: ${e.message}")

            // 创建一个简单的文本区域
            val simpleTextArea = JTextArea(editorDocument.text)
            simpleTextArea.font = com.intellij.util.ui.JBUI.Fonts.create("Monospaced", 12)
            simpleTextArea.document.addDocumentListener(object : javax.swing.event.DocumentListener {
                override fun insertUpdate(e: javax.swing.event.DocumentEvent) { modified = true }
                override fun removeUpdate(e: javax.swing.event.DocumentEvent) { modified = true }
                override fun changedUpdate(e: javax.swing.event.DocumentEvent) { modified = true }
            })

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
        commentLabel.border = com.intellij.util.ui.JBUI.Borders.empty(5)
        scriptPanel.add(commentLabel, BorderLayout.SOUTH)
        formPanel.add(scriptPanel)

        // 示例和应用按钮面板 - 使用标准按钮，兼容IDEA 2025.1
        val buttonPanel = com.intellij.ui.components.JBPanel<com.intellij.ui.components.JBPanel<*>>()
        buttonPanel.layout = FlowLayout(FlowLayout.LEFT)

        // 创建示例按钮
        exampleButton = JButton(LanguageBundle.message("settings.naming.style.example"))
        exampleButton.addActionListener { loadScriptExample() }
        exampleButton.icon = com.intellij.icons.AllIcons.Actions.Preview
        buttonPanel.add(exampleButton)

        // 创建应用按钮
        val applyButton = JButton(LanguageBundle.message("settings.naming.style.apply"))
        applyButton.addActionListener { applyChanges() }
        applyButton.icon = com.intellij.icons.AllIcons.Actions.Commit
        buttonPanel.add(applyButton)

        formPanel.add(buttonPanel)

        val editPanel = JPanel(BorderLayout())
        editPanel.add(formPanel, BorderLayout.CENTER)

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

        return mainPanel
    }

    private fun updateEditPanel() {
        val index = styleList.selectedIndex
        if (index >= 0 && index < settings.namingStyles.size) {
            val style = settings.namingStyles[index]
            nameField.text = style.name
            methodField.text = style.methodName
            // 确保脚本总是启用
            useScriptCheckBox.isSelected = true

            // 如果脚本内容为空，则根据方法名设置默认脚本
            if (style.scriptContent.isBlank()) {
                val defaultScript = when (style.methodName) {
                    "toCamelCase" -> com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getCamelCaseExample()
                    "toSnakeCase" -> com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getBasicExample()
                    "toUpperSnakeCase" -> com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getAdvancedExample()
                    "toPascalCase" -> com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getPascalCaseExample()
                    else -> com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider.getBasicExample()
                }
                scriptTextArea.text = defaultScript
                style.scriptContent = defaultScript
            } else {
                scriptTextArea.text = style.scriptContent
            }

            scriptTextArea.isEnabled = true
        } else {
            nameField.text = ""
            methodField.text = ""
            useScriptCheckBox.isSelected = true
            scriptTextArea.text = ""
            scriptTextArea.isEnabled = true
        }
    }

    /**
     * 创建Groovy脚本文件并启用代码补全
     */
    private fun createGroovyScriptFile(project: com.intellij.openapi.project.Project?): com.intellij.psi.PsiFile {
        if (project == null) {
            throw IllegalStateException("No project available")
        }

        // 获取Groovy文件类型
        val groovyFileType = com.intellij.openapi.fileTypes.FileTypeManager.getInstance().findFileTypeByName("Groovy")
                           ?: com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE

        // 默认脚本模板 - 包含完整的上下文以帮助代码补全
        val templateContent = """// Groovy脚本 - 用于命名风格转换
    // 可用变量:
    // - input: String - 输入字符串 (例如: user_name)

    import java.util.*

    // 定义输入变量
    def input = "sample_variable_name"

    // 您的命名转换逻辑:
    // 示例：将snake_case转换为camelCase
    def parts = input.split("_")
    def result = ""
    parts.eachWithIndex { part, index -> 
        if (index == 0) {
            result += part.toLowerCase()
        } else {
            result += part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase()
        }
    }
    return result
    """

        // 尝试使用真实文件以获得更好的代码补全支持
        try {
            if (project != com.intellij.openapi.project.ProjectManager.getInstance().defaultProject) {
                // 创建临时目录
                val projectBasePath = project.basePath
                if (projectBasePath != null) {
                    val tempDirPath = "$projectBasePath/.idea/namingStyles"
                    val tempDir = java.io.File(tempDirPath)
                    if (!tempDir.exists()) {
                        tempDir.mkdirs()
                    }

                    // 创建临时文件
                    val tempFile = java.io.File(tempDir, "NamingScriptEditor.groovy")
                    tempFile.writeText(templateContent)

                    // 刷新文件系统
                    val virtualFile = com.intellij.openapi.vfs.LocalFileSystem.getInstance().refreshAndFindFileByIoFile(tempFile)
                    if (virtualFile != null) {
                        // 确保使用Groovy语言
                        com.intellij.openapi.fileTypes.FileTypeManager.getInstance().associateExtension(groovyFileType, "groovy")

                        // 创建PSI文件
                        val psiManager = com.intellij.psi.PsiManager.getInstance(project)
                        val psiFile = psiManager.findFile(virtualFile)
                        if (psiFile != null) {
                            return psiFile
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // 记录错误但继续执行
            com.intellij.openapi.diagnostic.Logger.getInstance(NamingStyleConfigurable::class.java)
                .warn("创建物理文件失败: ${e.message}", e)
        }

        // 如果上述方法失败，回退到内存中的文件
        val psiFileFactory = com.intellij.psi.PsiFileFactory.getInstance(project)
        return psiFileFactory.createFileFromText("NamingScriptEditor.groovy", groovyFileType, templateContent)
    }

    /**
     * 创建编辑器
     */
    private fun createEditor(project: com.intellij.openapi.project.Project?, document: com.intellij.openapi.editor.Document): com.intellij.openapi.editor.Editor {
        val editorFactory = com.intellij.openapi.editor.EditorFactory.getInstance()

        // 获取Groovy文件类型
        val groovyFileType = com.intellij.openapi.fileTypes.FileTypeRegistry.getInstance().findFileTypeByName("Groovy")
                            ?: com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE

        // 创建编辑器，使用更完整的编辑器创建参数
        val editor = editorFactory.createEditor(
            document,
            project,
            groovyFileType,
            false /* 非只读 */
        )

        // 特别为Groovy启用额外的编辑器功能
        if (project != null) {
            val groovySupport = com.intellij.lang.Language.findLanguageByID("Groovy")
            if (groovySupport != null) {
                // 启用代码折叠
                com.intellij.codeInsight.folding.CodeFoldingManager.getInstance(project)?.updateFoldRegions(editor)

                // 启用代码补全 - 只使用IDEA 2025.1兼容的设置
                val groovyEditorOptions = editor.settings
                groovyEditorOptions.isSmartHome = true

                // 尝试设置其他可能的选项
                try {
                    // 使用反射尝试设置可能存在的属性
                    val settingsClass = groovyEditorOptions.javaClass

                    // 尝试设置参数名称提示
                    try {
                        val inlayHintsMethod = settingsClass.getMethod("setShowParameterNameHints", Boolean::class.java)
                        inlayHintsMethod.invoke(groovyEditorOptions, true)
                    } catch (e: NoSuchMethodException) {
                        // 忽略此错误，该选项可能在新版本中已重命名或移除
                    }

                    // 设置滚动选项
                    groovyEditorOptions.isRefrainFromScrolling = false
                    groovyEditorOptions.isAnimatedScrolling = true
                } catch (e: Exception) {
                    // 如果任何设置失败，仅记录而不中断流程
                    com.intellij.openapi.diagnostic.Logger.getInstance(NamingStyleConfigurable::class.java)
                        .debug("无法设置某些编辑器选项: ${e.message}")
                }
            }
        }

        return editor
    }

    // 这些复杂方法已被移除，相关功能直接整合到主要代码中

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
            "toCustomCase",
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

    private fun applyChanges() {
        val index = styleList.selectedIndex
        if (index >= 0) {
            val name = nameField.text.trim()
            val method = methodField.text.trim()
            val script = scriptTextArea.text

            if (name.isNotEmpty() && method.isNotEmpty()) {
                // 总是使用脚本
                settings.updateNamingStyle(index, name, method, true, script)
                // 更新列表
                styleListModel.setElementAt(settings.namingStyles[index], index)
                modified = true
            }
        }
    }

    /**
     * 加载脚本示例
     */
    private fun loadScriptExample() {
        val index = styleList.selectedIndex
        if (index < 0) return

        val exampleProvider = com.chenxinzhi.plugins.intellij.script.GroovyScriptExampleProvider

        // 创建示例项列表
        val examples = listOf(
            ExampleItem("camelCase 示例", exampleProvider::getCamelCaseExample),
            ExampleItem("PascalCase 示例", exampleProvider::getPascalCaseExample),
            ExampleItem("kebab-case 示例", exampleProvider::getAdvancedExample),
            ExampleItem("基本模板", exampleProvider::getBasicExample)
        )

        // 使用简单的弹出菜单，兼容IDEA 2025.1
        val popup = JPopupMenu()

        examples.forEach { example ->
            val menuItem = JMenuItem(example.name)
            menuItem.addActionListener {
                val scriptContent = example.contentProvider.invoke()

                // 应用到编辑器和文本区域
                com.intellij.openapi.application.ApplicationManager.getApplication().runWriteAction {
                    editorDocument.setText(scriptContent)
                    scriptTextArea.text = scriptContent
                }

                useScriptCheckBox.isSelected = true
            }
            popup.add(menuItem)
        }

        // 显示弹出菜单
        popup.show(exampleButton, 0, exampleButton.height)
    }

    // 用于示例项的辅助类
    private class ExampleItem(val name: String, val contentProvider: () -> String) {
        override fun toString(): String = name
    }

    // 添加示例按钮的引用
    private lateinit var exampleButton: javax.swing.JButton

    override fun isModified(): Boolean = modified

    override fun apply() {
        // 保存设置
        modified = false
    }

    override fun disposeUIResources() {
        com.intellij.openapi.util.Disposer.dispose(this)
    }

    override fun dispose() {
        // 释放所有编辑器资源
        if (::editor.isInitialized) {
            com.intellij.openapi.editor.EditorFactory.getInstance().releaseEditor(editor)
        }
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
