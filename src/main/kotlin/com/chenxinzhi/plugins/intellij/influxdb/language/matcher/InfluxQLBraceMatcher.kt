package com.chenxinzhi.plugins.intellij.influxdb.language.matcher

import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes
import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType

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
    BracePair(InfluxQLTypes.LPAREN, InfluxQLTypes.RPAREN, true),
)