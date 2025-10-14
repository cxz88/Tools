package com.chenxinzhi.plugins.intellij.reference

import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class TableNameReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        // 匹配所有字符串字面量
        registrar.registerReferenceProvider(
            PlatformPatterns.psiElement(PsiLiteralExpression::class.java),
            TableNameReferenceProvider()
        )
    }
}

class TableNameReferenceProvider : PsiReferenceProvider() {
    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        val literalExpr = element as? PsiLiteralExpression ?: return PsiReference.EMPTY_ARRAY

        // 检查是否是字符串字面量
        val value = literalExpr.value
        if (value !is String || value.isEmpty()) return PsiReference.EMPTY_ARRAY

        if (!isInTableNameAnnotation(literalExpr)) {
            return PsiReference.EMPTY_ARRAY
        }
        return arrayOf(TableNameReference(literalExpr, value))
    }

    private fun isInTableNameAnnotation(element: PsiElement): Boolean {
        var parent: PsiElement? = element.parent

        // 向上查找,直到找到注解或者超出范围
        var depth = 0
        while (parent != null && depth < 10) {
            if (parent is PsiAnnotation) {
                val qualifiedName = parent.qualifiedName
                return qualifiedName == "com.baomidou.mybatisplus.annotation.TableName"
            }
            // 如果已经超出注解的范围,停止查找
            if (parent is PsiClass || parent is PsiMethod || parent is PsiField) {
                break
            }
            parent = parent.parent
            depth++
        }

        return false
    }
}
