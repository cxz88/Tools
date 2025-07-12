package com.chenxinzhi.plugins.intellij.influxdb.language.completion

import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLIdentifierMy
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLMeasurement
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLSelectStatement
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLWhereClause
import com.chenxinzhi.plugins.intellij.utils.InfluxDBManager
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import kotlin.jvm.java

class InfluxQLCompletionContributor : CompletionContributor() {
    init {
        // 1. 关键字补全
        extend(CompletionType.BASIC, psiElement(), KeywordCompletionProvider())

        // 2. 表名 (Measurement) 补全
        extend(
            CompletionType.BASIC,
            psiElement().withParents(InfluxQLIdentifierMy::class.java, InfluxQLMeasurement::class.java),
            MeasurementCompletionProvider()
        )

        // 3. 字段名 (Field Key) 补全
        extend(
            CompletionType.BASIC,
            psiElement().withParents(InfluxQLIdentifierMy::class.java,InfluxQLWhereClause::class.java),
            FieldKeyCompletionProvider()
        )
    }

    private class KeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
        private val KEYWORDS = listOf(
            "SELECT", "FROM", "WHERE", "GROUP BY", "ORDER BY", "LIMIT", "SHOW", "DATABASES",
            "MEASUREMENTS", "FIELD KEYS", "TAG KEYS", "AND", "OR"
            // 添加更多关键字
        ).map { LookupElementBuilder.create(it).withBoldness(true) }

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            result.addAllElements(KEYWORDS)
        }
    }

    private class MeasurementCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val project = parameters.originalFile.project
            val dbManager = project.getService(InfluxDBManager::class.java)

            val measurements = dbManager.fetchMeasurements()
            measurements.forEach {
                result.addElement(LookupElementBuilder.create(it).withIcon(null))
            }
        }
    }

    private class FieldKeyCompletionProvider : CompletionProvider<CompletionParameters>() {
        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet
        ) {
            val position = parameters.position

            // 向上查找包含当前位置的 SELECT 语句
            val selectStatement = PsiTreeUtil.getParentOfType(position, InfluxQLSelectStatement::class.java) ?: return

            // 我们只在 SELECT 关键字之后，FROM 关键字之前的位置提示字段
            val fromClause = selectStatement.fromClause
            fromClause.measurementList.firstOrNull()?.let {
                val project = parameters.originalFile.project
                val dbManager = project.getService(InfluxDBManager::class.java)
                val fieldKeys = dbManager.fetchFieldKeys(it.name ?: "")
                fieldKeys.forEach {
                    result.addElement(LookupElementBuilder.create(it).withIcon(com.intellij.icons.AllIcons.Nodes.Field))
                }
            }
        }
    }
}