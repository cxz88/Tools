package com.chenxinzhi.plugins.intellij.influxdb.language.lexer

import com.intellij.lexer.FlexAdapter
import com.chenxinzhi.plugins.intellij.influxdb.language.psi._InfluxQLLexer

class InfluxQLLexerAdapter : FlexAdapter(_InfluxQLLexer(null))