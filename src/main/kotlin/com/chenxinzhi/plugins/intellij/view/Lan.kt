package com.chenxinzhi.plugins.intellij.view

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.TranService
import com.chenxinzhi.plugins.intellij.utils.getAllModules
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import localizeLiteralArgsUsingPsi
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextField
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.nio.file.Paths

@Composable
fun Lan(project: Project) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var moduleList by remember {
                mutableStateOf(
                    listOf(
                        GenData(
                            "", AllIconsKeys.Nodes.Module
                        )
                    )
                )
            }
            var enabled by remember {
                mutableStateOf(
                    false
                )
            }
            LaunchedEffect(Unit) {
                DumbService.getInstance(project).runWhenSmart {
                    moduleList = project.getAllModules().map {
                        GenData(
                            it.name,
                            AllIconsKeys.Nodes.Module,
                            (Paths.get(it.moduleFilePath).parent ?: "").toString(),
                            it
                        )
                    }.apply {
                        ifEmpty {
                            listOf(
                                GenData(
                                    "", AllIconsKeys.Nodes.Module
                                )
                            )
                        }
                    }
                    enabled = true
                }

            }
            var selectedIndex by remember { mutableIntStateOf(0) }
            ComboList(moduleList, selectedIndex, true) {
                selectedIndex = it
            }
            Spacer(modifier = Modifier.height(16.dp))
            val settingsService = remember { TranService.getInstance(project) }
            val appSecret = textFieldState()
            val appKey = textFieldState()
            val saveSettings = remember {
                {
                    settingsService.state.appKey = appSecret.text.toString()
                    settingsService.state.appId = appKey.text.toString()
                }
            }
            var first by remember { mutableStateOf(true) }
            LaunchedEffect(appKey.text, appSecret.text) {
                if (first) {
                    return@LaunchedEffect
                }
                saveSettings()
            }
            LaunchedEffect(Unit) {
                appSecret.setTextAndPlaceCursorAtEnd(settingsService.state.appKey)
                appKey.setTextAndPlaceCursorAtEnd(settingsService.state.appId)
                first = false
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${LanguageBundle.messagePointer("tran.app.key").get()}: ")
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = appKey,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${LanguageBundle.messagePointer("tran.app.secret").get()}: ")
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = appSecret,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            DefaultButton(enabled = enabled, onClick = {
                val data = moduleList[selectedIndex]
                data.module?.let { localizeLiteralArgsUsingPsi(project, modulePath = data.other, module = it,appSecret=appSecret.text.toString(),appKey=appKey.text.toString()) }
            }) {
                Text(LanguageBundle.messagePointer("tran.translate").get())
            }
        }
    }

}