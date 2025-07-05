package com.chenxinzhi.plugins.intellij.influxdb.language.psi

import com.intellij.psi.tree.IElementType
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import org.jetbrains.annotations.NonNls

/**
 * 自定义的词法符号（Token）类型。
 * 每个在 .flex 文件中定义的词法单元都会生成一个这种类型的实例。
 * 它将词法符号与我们的 InfluxQL 语言绑定。
 */
class InfluxQLTokenType(@NonNls debugName: String) : IElementType(debugName, InfluxQLLanguage)