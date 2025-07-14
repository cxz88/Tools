package com.chenxinzhi.plugins.intellij.model

data class InfluxDbProjectSettings(
    var influxUrl: String = "",
    var dbName: String = "",
    var user: String = "",
    var p: String = ""
)