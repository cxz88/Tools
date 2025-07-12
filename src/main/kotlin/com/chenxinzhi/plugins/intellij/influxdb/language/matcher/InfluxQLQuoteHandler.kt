package com.chenxinzhi.plugins.intellij.influxdb.language.matcher

import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes
import com.intellij.codeInsight.editorActions.QuoteHandler
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.highlighter.HighlighterIterator
import com.intellij.psi.PsiFile

class InfluxQLQuoteHandler : QuoteHandler {
    override fun isOpeningQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        val tokenType = iterator.tokenType
        return tokenType == InfluxQLTypes.SINGLE_QUOTES || tokenType == InfluxQLTypes.DOUBLE_QUOTES
    }

    override fun hasNonClosedLiteral(
        p0: Editor?,
        p1: HighlighterIterator?,
        p2: Int
    ): Boolean {
        return true
    }

    override fun isClosingQuote(iterator: HighlighterIterator, offset: Int): Boolean {
        return isOpeningQuote(iterator, offset)
    }

    override fun isInsideLiteral(iterator: HighlighterIterator): Boolean {
        return iterator.tokenType == InfluxQLTypes.IDENTIFIER
    }


}
