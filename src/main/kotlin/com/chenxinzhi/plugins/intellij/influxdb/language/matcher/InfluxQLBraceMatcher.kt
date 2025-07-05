package com.chenxinzhi.plugins.intellij.influxdb.language.matcher

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes

class InfluxQLBraceMatcher : PairedBraceMatcher {

    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean {
        return true
    }

    override fun getCodeConstructStart(file: PsiFile, openingBraceOffset: Int): Int {
        return openingBraceOffset
    }
}

private val PAIRS = arrayOf(
    // 第三个参数 isStructural 为 true，意味着这对括号会影响代码结构（例如代码折叠）
    BracePair(InfluxQLTypes.DOUBLE_QUOTES, InfluxQLTypes.DOUBLE_QUOTES, true),
    BracePair(InfluxQLTypes.SINGLE_QUOTES, InfluxQLTypes.SINGLE_QUOTES, true),
    BracePair(InfluxQLTypes.LPAREN, InfluxQLTypes.RPAREN, true)
)