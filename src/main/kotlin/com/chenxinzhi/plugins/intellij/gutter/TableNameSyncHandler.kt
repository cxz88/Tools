package com.chenxinzhi.plugins.intellij.gutter

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.utils.notifyError
import com.chenxinzhi.plugins.intellij.utils.notifySuccess
import com.google.common.base.CaseFormat
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.database.model.DataType
import com.intellij.database.model.ObjectKind
import com.intellij.database.psi.DbColumn
import com.intellij.database.psi.DbDataSourceImpl
import com.intellij.database.util.DbUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction.runWriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import java.awt.event.MouseEvent

class TableNameSyncHandler : GutterIconNavigationHandler<PsiElement> {
    override fun navigate(event: MouseEvent, element: PsiElement) {
        val project: Project = element.project
        val psiClass = element.context as? PsiClass ?: return

        ApplicationManager.getApplication().invokeLater {
            val dataSources = DbUtil.getDataSources(project).filterIsInstance<DbDataSourceImpl>()

            // 数据源选择对话框
            if (dataSources.isEmpty()) {
                return@invokeLater
            }

            // 使用 BaseListPopupStep 为弹窗准备数据源列表
            val dataSourceNames = dataSources.map { it.name }
            val popupStep =
                object : com.intellij.openapi.ui.popup.util.BaseListPopupStep<String>(
                    LanguageBundle.message("table.name.select.datasource"),
                    dataSourceNames
                ) {
                    override fun onChosen(
                        selectedValue: String?,
                        finalChoice: Boolean
                    ): com.intellij.openapi.ui.popup.PopupStep<*>? {
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

                        val tableName = psiClass.getAnnotation("com.baomidou.mybatisplus.annotation.TableName")
                            ?.findDeclaredAttributeValue("value")?.text ?: run {
                            return super.onChosen(selectedValue, finalChoice)
                        }

                        // 获取数据源下的所有数据库(schema)
                        val schemas = selectedDataSource.getDasChildren(ObjectKind.SCHEMA).toList()
                        if (schemas.isEmpty()) {
                            return super.onChosen(selectedValue, finalChoice)
                        }

                        // 返回数据库选择步骤
                        val schemaNames = schemas.map { it.name }
                        return object : com.intellij.openapi.ui.popup.util.BaseListPopupStep<String>(
                            LanguageBundle.message("table.name.select.database"),
                            schemaNames
                        ) {
                            override fun onChosen(
                                selectedSchema: String?,
                                finalChoice: Boolean
                            ): com.intellij.openapi.ui.popup.PopupStep<*>? {
                                if (selectedSchema == null) {
                                    return super.onChosen(selectedSchema, finalChoice)
                                }

                                val selectedSchemaObject = schemas.firstOrNull { it.name == selectedSchema }
                                if (selectedSchemaObject == null) {
                                    return super.onChosen(selectedSchema, finalChoice)
                                }

                                // 执行刷新和同步逻辑
                                refreshDatabaseAndSync(
                                    project,
                                    selectedSchemaObject,
                                    tableName,
                                    psiClass
                                )
                                return super.onChosen(selectedSchema, finalChoice)
                            }
                        }
                    }
                }

            // 在编辑器中弹出选择框
            val popup = com.intellij.openapi.ui.popup.JBPopupFactory.getInstance().createListPopup(popupStep)

            // 获取鼠标的坐标并以组件左上角为基准
            val pointRelativeToComponent = event.point

            // 将相对坐标转换为屏幕坐标
            val screenPoint = event.component.locationOnScreen
            val adjustedScreenPoint = screenPoint.apply {
                x += pointRelativeToComponent.x + 10 // 向右偏移 10 像素
                y += pointRelativeToComponent.y  // 保持与图标垂直对齐
            }

            // 弹出框在图标旁边展示
            popup.showInScreenCoordinates(event.component, adjustedScreenPoint)
        }
    }

    private fun refreshDatabaseAndSync(
        project: Project,
        schema: com.intellij.database.model.DasObject,
        tableName: String,
        psiClass: PsiClass
    ) {
        val tables = schema.getDasChildren(ObjectKind.TABLE).toList()
        val targetTable = tables.firstOrNull { it.name == tableName.trim('"') }

        if (targetTable == null) {
            project.notifyError(LanguageBundle.message("table.name.select.no.table"))
            return
        }

        val dbColumns = targetTable.getDasChildren(ObjectKind.COLUMN).toList()
        val factory = JavaPsiFacade.getElementFactory(project)
        runWriteCommandAction(project) {
            val allInheritedFields = mutableSetOf<String>()
            var currentClass = psiClass.superClass
            while (currentClass != null) {
                allInheritedFields.addAll(currentClass.fields.map { it.name })
                currentClass = currentClass.superClass
            }

            val existingFields = psiClass.fields.associateBy { it.name }

            existingFields.forEach { (fieldName, field) ->
                if (fieldName == "serialVersionUID") {
                    return@forEach
                }
                if (dbColumns.none {
                        CaseFormat.LOWER_UNDERSCORE.to(
                            CaseFormat.LOWER_CAMEL, it.name
                        ).equals(fieldName, ignoreCase = true)
                    }) {
                    field.delete()
                }
            }

            dbColumns.forEach { column ->
                val fieldName = CaseFormat.LOWER_UNDERSCORE.to(
                    CaseFormat.LOWER_CAMEL, column.name
                )
                if (!allInheritedFields.contains(fieldName) && !existingFields.containsKey(fieldName)) {
                    val comment = column.comment ?: column.name
                    (column as? DbColumn)?.dasType?.toDataType()?.let {
                        val fieldText = """
                        @io.swagger.annotations.ApiModelProperty(value = "$comment")
                        @com.baomidou.mybatisplus.annotation.TableField("${column.name}")
                        private ${getJavaType(it)} $fieldName;
                    """.trimIndent()

                        val field = factory.createFieldFromText(fieldText, psiClass)
                        psiClass.add(field)
                    }

                }
            }
            JavaCodeStyleManager.getInstance(project).shortenClassReferences(psiClass)
            project.notifySuccess(LanguageBundle.message("table.name.select.table.sync.success"))
        }
    }

    private fun getJavaType(column: DataType): String {
        column.let {
            val uppercase = it.typeName.uppercase()
            return when {
                uppercase.startsWith("VARCHAR") || uppercase == "CHAR" || uppercase.startsWith("TEXT") -> "String"
                uppercase in setOf("INT", "INTEGER", "SMALLINT", "TINYINT") -> "Integer"
                uppercase == "BIGINT" -> "Long"
                uppercase.startsWith("DECIMAL") || uppercase.startsWith("NUMERIC") || uppercase.startsWith("NUMBER") -> "java.math.BigDecimal"
                uppercase == "BOOLEAN" || uppercase == "BIT" -> "Boolean"
                uppercase.startsWith("DATETIME") || uppercase.startsWith("TIMESTAMP") -> "java.time.LocalDateTime"
                uppercase.startsWith("DATE") -> "java.time.LocalDate"
                uppercase.startsWith("TIME") -> "java.time.LocalTime"
                else -> "String"
            }
        }


    }


}
