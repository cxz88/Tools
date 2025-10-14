package com.chenxinzhi.plugins.intellij.reference

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.utils.notifyError
import com.intellij.database.editor.DatabaseEditorHelper
import com.intellij.database.model.ObjectKind
import com.intellij.database.psi.DbDataSourceImpl
import com.intellij.database.psi.DbTableImpl
import com.intellij.database.util.DbUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.FakePsiElement
import java.awt.MouseInfo
import java.awt.Point

class TableNameNavigableElement(
    private val parent: PsiElement,
    private val tableName: String
) : FakePsiElement(), Navigatable {
    override fun getName(): String {
        return tableName
    }
    override fun getParent(): PsiElement = parent

    override fun navigate(requestFocus: Boolean) {
        showDataSourceSelectionDialog()
    }

    override fun canNavigate(): Boolean = true

    override fun canNavigateToSource(): Boolean = true

    private fun showDataSourceSelectionDialog() {
        val project = parent.project
        ApplicationManager.getApplication().invokeLater {
            val dataSources = DbUtil.getDataSources(project).filterIsInstance<DbDataSourceImpl>()
            if (dataSources.isEmpty()) {
                return@invokeLater
            }
            // 创建数据源选择弹窗
            val dataSourceNames = dataSources.map { it.name }
            val popupStep =
                object : BaseListPopupStep<String>(LanguageBundle.message("table.name.select.datasource"), dataSourceNames) {
                    override fun onChosen(
                        selectedValue: String?,
                        finalChoice: Boolean
                    ): PopupStep<*>? {
                        if (selectedValue == null) {
                            return super.onChosen(selectedValue, finalChoice)
                        }

                        // 根据选择的名称找到具体的数据源
                        val selectedDataSource = dataSources.firstOrNull { it.name == selectedValue }
                        if (selectedDataSource == null) {
                            return super.onChosen(selectedValue, finalChoice)
                        }
                        if (selectedDataSource.isLoading) {
                            return super.onChosen(selectedValue, finalChoice)
                        }

                        // 获取数据源下的所有数据库(schema)
                        val schemas = selectedDataSource.getDasChildren(ObjectKind.SCHEMA).toList()
                        if (schemas.isEmpty()) {
                            return super.onChosen(selectedValue, finalChoice)
                        }

                        // 返回数据库选择步骤
                        val schemaNames = schemas.map { it.name }
                        return object : BaseListPopupStep<String>(LanguageBundle.message("table.name.select.database"), schemaNames) {
                            override fun onChosen(
                                selectedSchema: String?,
                                finalChoice: Boolean
                            ): PopupStep<*>? {
                                if (selectedSchema == null) {
                                    return super.onChosen(selectedSchema, finalChoice)
                                }

                                val selectedSchemaObject = schemas.firstOrNull { it.name == selectedSchema }
                                if (selectedSchemaObject == null) {
                                    return super.onChosen(selectedSchema, finalChoice)
                                }

                                // 执行刷新和同步逻辑
                                navigate(
                                    selectedSchemaObject,
                                    tableName,
                                )
                                return super.onChosen(selectedSchema, finalChoice)
                            }
                        }
                    }
                }

            // 在编辑器中弹出选择框
            val popup = JBPopupFactory.getInstance().createListPopup(popupStep)

            // 获取当前鼠标位置并在右侧显示弹窗
            val mouseLocation = MouseInfo.getPointerInfo()?.location
            if (mouseLocation != null) {
                // 获取当前编辑器组件
                val fileEditorManager = FileEditorManager.getInstance(project)
                val editorComponent = (fileEditorManager.selectedEditor as? TextEditor)?.editor?.component

                if (editorComponent != null) {
                    // 在鼠标位置右侧偏移显示
                    val popupLocation = Point(mouseLocation.x + 10, mouseLocation.y)
                    popup.showInScreenCoordinates(editorComponent, popupLocation)
                } else {
                    // 如果无法获取编辑器组件，则使用默认居中显示
                    popup.showCenteredInCurrentWindow(project)
                }
            } else {
                // 如果无法获取鼠标位置，则使用默认居中显示
                popup.showCenteredInCurrentWindow(project)
            }
        }
    }

    private fun navigate(
        schema: com.intellij.database.model.DasObject,
        tableName: String,
    ) {
        // 在选定的数据库中查找表
        val tables = schema.getDasChildren(ObjectKind.TABLE).toList()
        val targetTable = tables.firstOrNull { it.name == tableName.trim('"') }
        if (targetTable == null) {
            project.notifyError(LanguageBundle.message("table.name.select.no.table"))
            return
        }
        val element = targetTable as? DbTableImpl
        if (DatabaseEditorHelper.isTableDataAvailable(element)) {
            element?.navigate(true)
        }
    }
}
