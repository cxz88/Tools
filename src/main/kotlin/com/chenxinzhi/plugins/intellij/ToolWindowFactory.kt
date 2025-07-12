package com.chenxinzhi.plugins.intellij

import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.utils.InfluxDBManager
import com.chenxinzhi.plugins.intellij.view.GenCode
import com.chenxinzhi.plugins.intellij.view.InfluxDBPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.content.ContentManagerEvent
import com.intellij.ui.content.ContentManagerListener
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
        toolWindow.contentManager.addContentManagerListener(object : ContentManagerListener {
            override fun selectionChanged(event: ContentManagerEvent) {
                // 检查事件是否是“选中”操作，并且被选中的 content 是我们刚刚创建的那个
                if (event.operation == ContentManagerEvent.ContentOperation.add && event.content.isSelected) {
                    // 从 Content 中获取我们的 Panel 组件
                    val component = event.content.component
                    if (component is InfluxDBPanel) {
                        val dbManager = project.getService(InfluxDBManager::class.java)
                        dbManager.load()

                    }
                }
            }
        })

    }


}







