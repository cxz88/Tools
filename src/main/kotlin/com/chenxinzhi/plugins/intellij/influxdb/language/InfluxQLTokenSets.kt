package com.chenxinzhi.plugins.intellij.influxdb.language

import com.intellij.psi.tree.TokenSet
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes

interface InfluxQLTokenSets {
    companion object {
        val IDENTIFIERS: TokenSet = TokenSet.create(InfluxQLTypes.IDENTIFIER)
        val KEYWORDS: TokenSet = TokenSet.create(
            // ... (把 .bnf 文件中所有的关键字都列在这里)
            InfluxQLTypes.SELECT, InfluxQLTypes.FROM, InfluxQLTypes.WHERE, InfluxQLTypes.GROUP,
            InfluxQLTypes.BY, InfluxQLTypes.ORDER, InfluxQLTypes.LIMIT, InfluxQLTypes.SHOW,
            InfluxQLTypes.DATABASES, InfluxQLTypes.MEASUREMENTS
            // ... etc
        )
        val COMMENTS: TokenSet = TokenSet.create(InfluxQLTypes.COMMENT)
    }
}