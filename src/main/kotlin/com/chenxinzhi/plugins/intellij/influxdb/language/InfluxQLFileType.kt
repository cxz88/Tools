package com.chenxinzhi.plugins.intellij.influxdb.language

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

// 如果你有图标，可以替换 Icons.INFLUX_ICON
object InfluxQLFileType : LanguageFileType(InfluxQLLanguage) {
    override fun getName() = "InfluxQL File"
    override fun getDescription() = "InfluxDB query language file"
    override fun getDefaultExtension() = "influxql"
    override fun getIcon(): Icon? = null // TODO: Add an icon
}