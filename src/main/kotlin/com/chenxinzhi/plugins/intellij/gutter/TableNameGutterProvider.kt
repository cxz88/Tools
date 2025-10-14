package com.chenxinzhi.plugins.intellij.gutter

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.FunctionUtil

class TableNameGutterProvider : LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        // 获取当前 PsiElement 所在的类（仅过滤为类或接口定义）
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java, false) ?: return null

        // 检查类是否标注了 @TableName 注解
        psiClass.modifierList?.findAnnotation("com.baomidou.mybatisplus.annotation.TableName")
            ?: // 如果没有 @TableName 注解，返回 null
            return null

        // 确保当前 PsiElement 是类的名称部分
        if (element != psiClass.nameIdentifier) return null

        // 为类名生成 Gutter 图标
        return LineMarkerInfo(
            element,
            element.textRange,
            AllIcons.Actions.Refresh, // 使用 IDEA 内置的刷新图标
            FunctionUtil.constant(LanguageBundle.message("table.name.select.table.sync")),
            TableNameSyncHandler(),
            GutterIconRenderer.Alignment.LEFT
        ) { LanguageBundle.message("table.name.select.table.sync") }
    }
}

