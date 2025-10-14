package com.chenxinzhi.plugins.intellij.reference

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiReferenceBase

class TableNameReference(
    element: PsiLiteralExpression,
    private val tableName: String
) : PsiReferenceBase<PsiLiteralExpression>(element, getTextRange(element), false) {

    companion object {
        private fun getTextRange(element: PsiLiteralExpression): TextRange {
            val text = element.text
            // 去除引号,计算实际的文本范围
            return if (text.startsWith("\"") && text.endsWith("\"")) {
                TextRange(1, text.length - 1)
            } else {
                TextRange(0, text.length)
            }
        }
    }
    override fun resolve(): PsiElement {
        return TableNameNavigableElement(element, tableName)
    }
}
