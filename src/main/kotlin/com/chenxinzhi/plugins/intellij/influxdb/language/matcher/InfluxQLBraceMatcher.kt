package com.chenxinzhi.plugins.intellij.influxdb.language.matcher

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes

class InfluxQLBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    // 这个方法可以保持原样，因为它对于括号配对依然有效
    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true // 括号几乎可以在任何地方配对
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}

// 定义配对，这次使用正确的 IElementType
private val PAIRS = arrayOf(
    // 第三个参数 isStructural 为 true，意味着这对括号会影响代码结构（例如代码折叠）
    BracePair(InfluxQLTypes.LPAREN, InfluxQLTypes.RPAREN, true)
)