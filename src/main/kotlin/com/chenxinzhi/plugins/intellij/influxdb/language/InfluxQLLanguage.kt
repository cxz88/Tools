package com.chenxinzhi.plugins.intellij.influxdb.language

import com.intellij.lang.Language

object InfluxQLLanguage : Language("InfluxQL") {
    private fun readResolve(): Any = InfluxQLLanguage
    override fun isCaseSensitive() = false // InfluxQL keywords are case-insensitive
}