package com.chenxinzhi.plugins.intellij.influxdb.language

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.chenxinzhi.plugins.intellij.influxdb.language.lexer.InfluxQLLexerAdapter
import com.chenxinzhi.plugins.intellij.influxdb.language.parser.InfluxQLParser
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLFile
import com.chenxinzhi.plugins.intellij.influxdb.language.psi.InfluxQLTypes

class InfluxQLParserDefinition : ParserDefinition {
    override fun createLexer(project: Project) = InfluxQLLexerAdapter

    override fun createParser(project: Project) = InfluxQLParser()

    override fun getFileNodeType() = FILE

    override fun getCommentTokens(): TokenSet = InfluxQLTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = InfluxQLTokenSets.IDENTIFIERS

    override fun createElement(node: ASTNode): PsiElement = InfluxQLTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = InfluxQLFile(viewProvider)

}

val FILE = IFileElementType(InfluxQLLanguage)