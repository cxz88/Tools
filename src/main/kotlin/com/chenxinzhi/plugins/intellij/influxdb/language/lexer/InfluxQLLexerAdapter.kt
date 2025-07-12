package com.chenxinzhi.plugins.intellij.influxdb.language.lexer

import com.chenxinzhi.plugins.intellij.influxdb.language.psi._InfluxQLLexer
import com.intellij.lexer.FlexAdapter

object InfluxQLLexerAdapter : FlexAdapter(_InfluxQLLexer())