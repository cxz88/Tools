package com.chenxinzhi.plugins.intellij.influxdb.language.psi

import com.intellij.psi.tree.IElementType
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import org.jetbrains.annotations.NonNls

/**
 * 自定义的 PSI 元素类型。
 * 每个在 .bnf 文件中定义的规则（非 private）都会生成一个这种类型的实例。
 * 它将 PSI 节点与我们的 InfluxQL 语言绑定。
 */
class InfluxQLElementType(@NonNls debugName: String) : IElementType(debugName, InfluxQLLanguage)