package com.chenxinzhi.plugins.intellij.influxdb.language.completion

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLFromClause
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLMeasurement
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLSelectStatement
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes
import com.chenxinzhi.plugins.intellij.utils.InfluxDBManager

import kotlin.jvm.java

class InfluxQLCompletionContributor : CompletionContributor() {
    init {
        // 1. 关键字补全
        extend(CompletionType.BASIC, psiElement(), KeywordCompletionProvider())

        // 2. 表名 (Measurement) 补全
        extend(CompletionType.BASIC, psiElement(InfluxQLTypes.IDENTIFIER).withParent(InfluxQLFromClause::class.java), MeasurementCompletionProvider())

        // 3. 字段名 (Field Key) 补全
        extend(CompletionType.BASIC, psiElement(InfluxQLTypes.IDENTIFIER), FieldKeyCompletionProvider())
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        private val KEYWORDS = listOf(
            "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT", "SHOW", "DATABASES",
            "MEASUREMENTS", "FIELD KEYS", "TAG KEYS", "CREATE", "DROP", "DELETE", "AND", "OR"
            // 添加更多关键字
        ).map { LookupElementBuilder.create(it).withBoldness(true) }

        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            result.addAllElements(KEYWORDS)
        }
    }

    private class MeasurementCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val project = parameters.originalFile.project
            val dbManager = project.getService(InfluxDBManager::class.java)

            val measurements = dbManager.fetchMeasurements()
            measurements.forEach {
                result.addElement(LookupElementBuilder.create(it).withIcon(null))
            }
        }
    }

    private class FieldKeyCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
            val position = parameters.position

            // 向上查找包含当前位置的 SELECT 语句
            val selectStatement = PsiTreeUtil.getParentOfType(position, InfluxQLSelectStatement::class.java) ?: return

            // 我们只在 SELECT 关键字之后，FROM 关键字之前的位置提示字段
            val fromClause = selectStatement.fromClause ?: return
            if (position.textOffset > fromClause.textOffset) {
                // 如果光标在 FROM 之后，则不提示字段名（可以在 WHERE 子句中做更复杂的判断）
                return
            }

            val project = parameters.originalFile.project
            val dbManager = project.getService(InfluxDBManager::class.java)

            // 从 FROM 子句中找到所有的表名
            val measurements = selectStatement.fromClause?.measurementList ?: emptyList()
            if (measurements.isNotEmpty()) {
                // 为简化，我们只取第一个表的字段
                val firstMeasurementName = measurements.first().name ?: return

                val fieldKeys = dbManager.fetchFieldKeys(firstMeasurementName)
                fieldKeys.forEach {
                    result.addElement(LookupElementBuilder.create(it).withIcon(com.intellij.icons.AllIcons.Nodes.Field))
                }
            }
        }
    }
}