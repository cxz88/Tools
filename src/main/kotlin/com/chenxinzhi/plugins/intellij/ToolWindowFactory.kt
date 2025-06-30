package com.chenxinzhi.plugins.intellij

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.view.GenCode
import com.chenxinzhi.plugins.intellij.view.InfluxDBPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.jewel.foundation.ExperimentalJewelApi


@Suppress("unused")
@ExperimentalCoroutinesApi
class ToolWindowFactory : ToolWindowFactory, DumbAware {
    @OptIn(ExperimentalJewelApi::class)
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        toolWindow.addComposeTab(LanguageBundle.messagePointer("tool.gen.name").get(), isLockable = false) {
            GenCode(project)
        }
        val panel = InfluxDBPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, LanguageBundle.messagePointer("tool.influxDb.name").get(), false)
        toolWindow.contentManager.addContent(content)

    }


}







