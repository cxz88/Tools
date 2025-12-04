@file:Suppress("DuplicatedCode")

package com.chenxinzhi.plugins.intellij.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.QRCodeSettingsService
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import org.jetbrains.jewel.ui.component.*
import java.awt.image.BufferedImage

/**
 * 二维码生成和展示面板
 * @author chenxinzhi
 * @date 2025-12-04
 */
@Composable
fun QRCodePanel(project: Project) {
    val textState = remember { TextFieldState("") }
    var qrCodeImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var qrCodeSize by remember { mutableIntStateOf(300) }

    // 获取持久化设置服务
    val settingsService = remember { QRCodeSettingsService.getInstance(project) }
    val settings = remember { settingsService.state }
    var first by remember { mutableStateOf(true) }

    // 保存设置
    val saveSettings = remember {
        {
            if (!first) {
                settings.text = textState.text.toString()
                settings.size = qrCodeSize
            }
        }
    }

    // 加载保存的设置
    LaunchedEffect(Unit) {
        textState.setTextAndPlaceCursorAtEnd(settings.text)
        qrCodeSize = settings.size
        if (settings.text.isNotEmpty()) {
            qrCodeImage = generateQRCode(settings.text, settings.size)
        }
        first = false
    }

    // 监听文本和尺寸变化,实时生成二维码
    LaunchedEffect(textState.text, qrCodeSize) {
        val text = textState.text.toString()
        qrCodeImage = if (text.isNotEmpty()) {
            generateQRCode(text, qrCodeSize)
        } else {
            null
        }
        saveSettings()
    }

    // 左右布局
    Row(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左侧：输入区域
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                LanguageBundle.messagePointer("tool.qrcode.input").get(),
                style = Typography.h3TextStyle()
            )

            TextField(
                state = textState,
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text(LanguageBundle.messagePointer("tool.qrcode.placeholder").get()) }
            )

            // 尺寸选择
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(LanguageBundle.messagePointer("tool.qrcode.size").get())

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(200, 300, 400, 500).forEach { size ->
                        RadioButtonRow(
                            selected = qrCodeSize == size,
                            onClick = { qrCodeSize = size }
                        ) {
                            Text("${size}px")
                        }
                    }
                }
            }
        }

        // 右侧：二维码显示区域
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            qrCodeImage?.let { image ->
                Image(
                    bitmap = image,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(qrCodeSize.dp)
                )
            } ?: run {
                Text(
                    LanguageBundle.messagePointer("tool.qrcode.placeholder").get(),
                    style = Typography.h4TextStyle()
                )
            }
        }
    }
}

/**
 * 使用 ZXing 生成二维码图片
 */
private fun generateQRCode(text: String, size: Int): ImageBitmap {
    try {
        // 配置二维码参数
        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.MARGIN to 1
        )

        // 使用 ZXing 生成二维码矩阵
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints)

        // 将矩阵转换为图片
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        for (x in 0 until size) {
            for (y in 0 until size) {
                val color = if (bitMatrix.get(x, y)) JBColor.BLACK.rgb else JBColor.WHITE.rgb
                image.setRGB(x, y, color)
            }
        }

        return image.toComposeImageBitmap()
    } catch (e: Exception) {
        // 如果生成失败，返回空白图片
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        val graphics = image.createGraphics()
        graphics.color = JBColor.WHITE
        graphics.fillRect(0, 0, size, size)
        graphics.dispose()
        return image.toComposeImageBitmap()
    }
}
