package com.chenxinzhi.plugins.intellij.influxdb.language.psi

import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLFileType
import com.chenxinzhi.plugins.intellij.influxdb.language.InfluxQLLanguage
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class InfluxQLFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, InfluxQLLanguage) {
    override fun getFileType(): FileType {
        return InfluxQLFileType
    }

    override fun toString(): String {
        return "InfluxQL File"
    }
}