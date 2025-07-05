package com.chenxinzhi.plugins.intellij.influxdb.language.highlighter

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLTokenSets
import com.chenxinzhi.plugins.intellij.influxdb.language.lexer.InfluxQLLexerAdapter
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes

class InfluxQLSyntaxHighlighter : SyntaxHighlighterBase() {

    companion object {
        val KEYWORD = createTextAttributesKey("INFLUXQL_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val IDENTIFIER = createTextAttributesKey("INFLUXQL_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val STRING = createTextAttributesKey("INFLUXQL_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER = createTextAttributesKey("INFLUXQL_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val COMMENT = createTextAttributesKey("INFLUXQL_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val OPERATOR = createTextAttributesKey("INFLUXQL_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val PARENTHESES = createTextAttributesKey("INFLUXQL_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BAD_CHARACTER = createTextAttributesKey("INFLUXQL_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)
    }

    override fun getHighlightingLexer(): Lexer = InfluxQLLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> {
        return when {
            InfluxQLTokenSets.KEYWORDS.contains(tokenType) -> arrayOf(KEYWORD)
            tokenType == InfluxQLTypes.IDENTIFIER -> arrayOf(IDENTIFIER)
            tokenType == InfluxQLTypes.STRING_LITERAL -> arrayOf(STRING)
            tokenType == InfluxQLTypes.NUMBER_LITERAL -> arrayOf(NUMBER)
            tokenType == InfluxQLTypes.COMMENT -> arrayOf(COMMENT)
            tokenType == InfluxQLTypes.LPAREN || tokenType == InfluxQLTypes.RPAREN -> arrayOf(PARENTHESES)
            // 你可以继续添加其他操作符
            tokenType == InfluxQLTypes.EQ || tokenType == InfluxQLTypes.GT || tokenType == InfluxQLTypes.LT -> arrayOf(OPERATOR)
            tokenType == com.intellij.psi.TokenType.BAD_CHARACTER -> arrayOf(BAD_CHARACTER)
            else -> emptyArray()
        }
    }
}