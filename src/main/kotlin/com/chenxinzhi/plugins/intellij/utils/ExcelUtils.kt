package com.chenxinzhi.plugins.intellij.utils

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Excel工具类，用于导出和导入翻译内容
 */
object ExcelUtils {

    /**
     * 将中文字符串列表导出到Excel
     * @param chineseTexts 中文文本列表
     * @param outputFile 输出文件
     */
    fun exportToExcel(chineseTexts: List<String>, outputFile: File) {
        val workbook: Workbook = XSSFWorkbook()
        val sheet: Sheet = workbook.createSheet("翻译内容")

        // 创建标题行
        val headerRow: Row = sheet.createRow(0)
        val headerStyle = workbook.createCellStyle().apply {
            val font = workbook.createFont()
            font.bold = true
            setFont(font)
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }



        val chineseHeader = headerRow.createCell(0)
        chineseHeader.setCellValue("中文")
        chineseHeader.cellStyle = headerStyle

        val translationHeader = headerRow.createCell(1)
        translationHeader.setCellValue("翻译（请填写）")
        translationHeader.cellStyle = headerStyle

        // 填充数据
        chineseTexts.forEachIndexed { index, text ->
            val row = sheet.createRow(index + 1)
            // Key列：使用拼音
            // 中文列
            row.createCell(0).setCellValue(text)
            // 翻译列（空白，待填写）
            row.createCell(1).setCellValue("")
        }

        // 自动调整列宽
        for (i in 0..1) {
            sheet.autoSizeColumn(i)
            // 增加一些额外宽度
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 2000)
        }

        // 写入文件
        FileOutputStream(outputFile).use { fileOut ->
            workbook.write(fileOut)
        }
        workbook.close()
    }

    /**
     * 从Excel导入翻译内容
     * @param inputFile 输入文件
     * @return 中文到翻译的映射
     */
    fun importFromExcel(inputFile: File): Map<String, String> {
        val translations = mutableMapOf<String, String>()

        FileInputStream(inputFile).use { fis ->
            val workbook: Workbook = XSSFWorkbook(fis)
            val sheet: Sheet = workbook.getSheetAt(0)

            // 跳过标题行，从第二行开始读取
            for (i in 1..sheet.lastRowNum) {
                val row: Row? = sheet.getRow(i)
                if (row != null) {
                    val chineseCell = row.getCell(0)
                    val translationCell = row.getCell(1)

                    val chinese = getCellValueAsString(chineseCell)
                    val translation = getCellValueAsString(translationCell)

                    // 只添加有效的翻译（中文和翻译都不为空）
                    if (chinese.isNotBlank() && translation.isNotBlank()) {
                        translations[chinese] = translation
                    }
                }
            }
            workbook.close()
        }

        return translations
    }

    /**
     * 获取单元格的字符串值
     */
    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""

        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    cell.dateCellValue.toString()
                } else {
                    cell.numericCellValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> cell.cellFormula
            else -> ""
        }
    }

    /**
     * 将中文转换为拼音（小写，点号分隔）
     * @param chinese 中文字符串
     * @return 拼音字符串
     */
    fun chineseToPinyin(chinese: String): String {
        if (chinese.isBlank()) return ""

        val format = HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.LOWERCASE
            toneType = HanyuPinyinToneType.WITHOUT_TONE
            vCharType = HanyuPinyinVCharType.WITH_V
        }

        val pinyinList = mutableListOf<String>()

        for (char in chinese) {
            if (char.isWhitespace()) {
                continue
            }

            when {
                // 中文字符
                char.toString().matches(Regex("[\\u4e00-\\u9fa5]")) -> {
                    try {
                        val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                        if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                            pinyinList.add(pinyinArray[0])
                        } else {
                            pinyinList.add(char.toString())
                        }
                    } catch (e: Exception) {
                        pinyinList.add(char.toString())
                    }
                }
                // 英文字母或数字
                char.isLetterOrDigit() -> {
                    pinyinList.add(char.lowercase())
                }
                // 其他字符忽略
                else -> {}
            }
        }

        return pinyinList.joinToString(".")
    }
}
