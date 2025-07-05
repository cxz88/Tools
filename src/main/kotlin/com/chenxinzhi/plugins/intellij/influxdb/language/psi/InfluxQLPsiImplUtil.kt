package com.chenxinzhi.plugins.intellij.influxdb.language.psi

object InfluxQLPsiImplUtil {
    @JvmStatic
    fun getName(element: InfluxQLMeasurement): String? {
        // 名称可能是带引号的标识符或不带引号的。
        // PSI 树中的 InfluxQLIdentifier 节点包含了文本。
        val identifier = element.identifierMy
        var text = identifier.text
        if (text.startsWith("\"") && text.endsWith("\"")) {
            text = text.substring(1, text.length - 1)
        }
        return text
    }
}