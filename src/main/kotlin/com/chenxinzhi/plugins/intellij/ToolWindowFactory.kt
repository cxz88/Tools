package com.chenxinzhi.plugins.intellij

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.view.GenCode
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.foundation.ExperimentalJewelApi


@Suppress("unused")
@ExperimentalCoroutinesApi
class ToolWindowFactory : ToolWindowFactory, DumbAware {
    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab(LanguageBundle.messagePointer("tool.gen.name").get()) {
            GenCode(project)
        }

    }


}







