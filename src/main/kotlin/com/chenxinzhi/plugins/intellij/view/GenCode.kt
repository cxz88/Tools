@file:Suppress("DuplicatedCode")

package com.chenxinzhi.plugins.intellij.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import com.chenxinzhi.plugins.intellij.language.LanguageBundle
import com.chenxinzhi.plugins.intellij.services.GenCodeProjectSettingsService
import com.chenxinzhi.plugins.intellij.utils.*
import com.intellij.database.access.DatabaseCredentials
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.model.DasNamespace
import com.intellij.database.model.DasObject
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtil
import kotlinx.coroutines.delay
import org.jetbrains.jewel.foundation.ExperimentalJewelApi
import org.jetbrains.jewel.ui.component.*
import org.jetbrains.jewel.ui.icon.IconKey
import org.jetbrains.jewel.ui.icons.AllIconsKeys
import java.nio.file.Paths

/**
 * @author chenxinzhi
 * @date 2025-06-27 09:21:25
 */

@Composable
fun GenCode(project: Project) {
    var dataSources: List<LocalDataSource> by remember { mutableStateOf(listOf()) }
    var devModules: List<Module> by remember { mutableStateOf(listOf()) }
    var dataBases: List<DasNamespace> by remember { mutableStateOf(listOf()) }
    var tables: List<DasObject> by remember { mutableStateOf(listOf()) }

    var nowDataSource: LocalDataSource? by remember { mutableStateOf(null) }
    var nowDataBase: DasNamespace? by remember { mutableStateOf(null) }
    var nowTable: DasObject? by remember { mutableStateOf(null) }
    var devModule: Module? by remember { mutableStateOf(null) }
    var allModule: List<Module>? by remember { mutableStateOf(null) }

    var nowServicePath: String? by remember { mutableStateOf(null) }
    var nowServiceApiPath: String? by remember { mutableStateOf(null) }

    val menuState = textFieldState()
    val serviceNameState = textFieldState()
    val tablePreState = textFieldState()
    val webPreState = textFieldState()
    val code = textFieldState()
    val fucCode = textFieldState()
    val packageName = textFieldState()
    val frontDir = textFieldState()

    var baseMode by remember { mutableStateOf(false) }
    var tenantMode by remember { mutableStateOf(false) }
    var useElementUI by remember { mutableStateOf(false) }
    var wrapMode by remember { mutableStateOf(false) }
    var buE by remember { mutableStateOf(true) }
    var msg by remember { mutableStateOf("") }

    // 获取持久化设置服务
    val settingsService = remember { GenCodeProjectSettingsService.getInstance(project) }
    val settings = remember { settingsService.state }
    var first by remember { mutableStateOf(true) }
    val saveSettings = remember {
        {
            if (first) {
                return@remember
            }
            settings.menuText = menuState.text.toString()
            settings.serviceNameText = serviceNameState.text.toString()
            settings.tablePrefixText = tablePreState.text.toString()
            settings.webPrefixText = webPreState.text.toString()
            settings.codeText = code.text.toString()
            settings.fucCodeText = fucCode.text.toString()
            settings.packageNameText = packageName.text.toString()
            settings.frontDirText = frontDir.text.toString()
            settings.baseMode = baseMode
            settings.tenantMode = tenantMode
            settings.useElementUI = useElementUI
            settings.wrapMode = wrapMode
            settings.servicePath = nowServicePath ?: ""
            settings.serviceApiPath = nowServiceApiPath ?: ""

            // 保存数据源和数据库名称（如果存在）
            nowDataSource?.let { settings.dataSourceName = it.name }
            nowDataBase?.let { settings.databaseName = it.name }
            nowTable?.let { settings.tableName = it.name }
            devModule?.let { settings.devModuleName = it.name }
        }

    }
    LaunchedEffect(Unit) {
        menuState.setTextAndPlaceCursorAtEnd(settings.menuText)
        serviceNameState.setTextAndPlaceCursorAtEnd(settings.serviceNameText)
        tablePreState.setTextAndPlaceCursorAtEnd(settings.tablePrefixText)
        webPreState.setTextAndPlaceCursorAtEnd(settings.webPrefixText)
        code.setTextAndPlaceCursorAtEnd(settings.codeText)
        fucCode.setTextAndPlaceCursorAtEnd(settings.fucCodeText)
        packageName.setTextAndPlaceCursorAtEnd(settings.packageNameText)
        frontDir.setTextAndPlaceCursorAtEnd(settings.frontDirText)

        baseMode = settings.baseMode
        tenantMode = settings.tenantMode
        useElementUI = settings.useElementUI
        wrapMode = settings.wrapMode

        nowServicePath = settings.servicePath
        nowServiceApiPath = settings.serviceApiPath
        first = false
    }
    LaunchedEffect(
        menuState.text,
        serviceNameState.text,
        tablePreState.text,
        webPreState.text,
        code.text,
        fucCode.text,
        packageName.text,
        frontDir.text,
        baseMode,
        tenantMode,
        useElementUI,
        wrapMode,
        nowServicePath,
        nowServiceApiPath
    ) { saveSettings() }
    Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {


        Spacer(modifier = Modifier.height(8.dp))
        Text(
            LanguageBundle.messagePointer("tool.gen.select.dev.text").get(),
            style = Typography.h3TextStyle(),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            var devModuleList by remember {
                mutableStateOf(
                    listOf(
                        GenData(
                            "", AllIconsKeys.Nodes.Module
                        )
                    )
                )
            }
            var isLoad by remember {
                mutableStateOf(
                    false
                )
            }
            var selectedIndex by remember { mutableIntStateOf(0) }

            LaunchedEffect(Unit) {
                devModuleList = project.getAllModules().filter { it.name.contains("dev") }.apply {
                    devModules = this
                }.map {
                    GenData(
                        it.name, AllIconsKeys.Nodes.Module
                    )
                }.let {
                    if (it.isNotEmpty()) {
                        isLoad = true
                        // 尝试根据保存的名称恢复选择
                        val savedModuleName = settings.devModuleName
                        val moduleIndex = if (savedModuleName.isNotEmpty()) {
                            devModules.indexOfFirst { m -> m.name == savedModuleName }
                        } else -1

                        selectedIndex = if (moduleIndex >= 0) moduleIndex else 0
                        devModule = devModules[selectedIndex]
                    }
                    it.ifEmpty {
                        listOf(
                            GenData(
                                "", AllIconsKeys.Nodes.Module
                            )
                        )
                    }
                }
            }

            ComboList(devModuleList, selectedIndex, isLoad) {
                selectedIndex = it
                devModule = devModules[it]
            }


        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.select.datasource").get())
                var dateSourceList by remember {
                    mutableStateOf(
                        listOf(
                            GenData(
                                "", AllIconsKeys.Providers.Mysql
                            )
                        )
                    )
                }
                var isLoad by remember {
                    mutableStateOf(
                        false
                    )
                }
                var selectedIndex by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    dateSourceList = project.getAllDataSources().apply {
                        dataSources = this
                    }.map {
                        GenData(
                            it.dataSource.name, AllIconsKeys.Providers.Mysql
                        )
                    }.let {
                        if (it.isNotEmpty()) {
                            isLoad = true
                            // 尝试根据保存的名称恢复选择
                            val savedDataSourceName = settings.dataSourceName
                            val dataSourceIndex = if (savedDataSourceName.isNotEmpty()) {
                                dataSources.indexOfFirst { ds -> ds.name == savedDataSourceName }
                            } else -1

                            selectedIndex = if (dataSourceIndex >= 0) dataSourceIndex else 0
                            nowDataSource = dataSources[selectedIndex]
                        }
                        it.ifEmpty {
                            nowDataSource = null
                            listOf(
                                GenData(
                                    "", AllIconsKeys.Providers.Mysql
                                )
                            )

                        }
                    }
                }

                ComboList(dateSourceList, selectedIndex, isLoad) {
                    selectedIndex = it
                    nowDataSource = dataSources[it]
                    saveSettings()
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.select.service").get())
                var moduleList by remember {
                    mutableStateOf(
                        listOf(
                            GenData(
                                "", AllIconsKeys.Nodes.Module
                            )
                        )
                    )
                }
                var isLoad by remember {
                    mutableStateOf(
                        false
                    )
                }
                var selectedIndex by remember { mutableIntStateOf(0) }
                LaunchedEffect(Unit) {
                    moduleList = project.getAllModules().apply {
                        allModule = this
                    }.map {
                        GenData(
                            it.name, AllIconsKeys.Nodes.Module, (Paths.get(it.moduleFilePath).parent ?: "").toString()
                        )
                    }.let {
                        if (it.isNotEmpty()) {
                            isLoad = true
                            // 尝试根据保存的路径恢复选择
                            val savedServicePath = settings.servicePath
                            val servicePathIndex = if (savedServicePath.isNotEmpty()) {
                                it.indexOfFirst { m -> m.other == savedServicePath }
                            } else -1

                            selectedIndex = if (servicePathIndex >= 0) servicePathIndex else 0
                            nowServicePath = it[selectedIndex].other
                        }
                        it.ifEmpty {
                            listOf(
                                GenData(
                                    "", AllIconsKeys.Nodes.Module
                                )
                            )
                        }
                    }
                }

                ComboList(moduleList, selectedIndex, isLoad) {
                    selectedIndex = it
                    nowServicePath = moduleList[selectedIndex].other
                    saveSettings()
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.select.serviceApi").get())
                var apiModuleList by remember {
                    mutableStateOf(
                        listOf(
                            GenData(
                                "",
                                AllIconsKeys.Nodes.Module,

                                )
                        )
                    )
                }
                var isLoad by remember {
                    mutableStateOf(
                        false
                    )
                }
                var selectedIndex by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    apiModuleList = project.getAllModules().filter { it.name.contains("api") }.map {
                        GenData(
                            it.name, AllIconsKeys.Nodes.Module, (Paths.get(it.moduleFilePath).parent ?: "").toString()
                        )
                    }.let {
                        if (it.isNotEmpty()) {
                            isLoad = true
                            // 尝试根据保存的路径恢复选择
                            val savedApiPath = settings.serviceApiPath
                            val apiPathIndex = if (savedApiPath.isNotEmpty()) {
                                it.indexOfFirst { m -> m.other == savedApiPath }
                            } else -1

                            selectedIndex = if (apiPathIndex >= 0) apiPathIndex else 0
                            nowServiceApiPath = it[selectedIndex].other
                        }
                        it.ifEmpty {
                            listOf(
                                GenData(
                                    "",
                                    AllIconsKeys.Nodes.Module,

                                    )
                            )
                        }
                    }
                }

                ComboList(apiModuleList, selectedIndex, isLoad) {
                    selectedIndex = it
                    nowServiceApiPath = apiModuleList[selectedIndex].other
                    saveSettings()
                }


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.select.database").get())
                var dateBaseList by remember {
                    mutableStateOf(
                        listOf(
                            GenData(
                                "", AllIconsKeys.Nodes.DataSchema

                            )
                        )
                    )
                }
                var isLoad by remember {
                    mutableStateOf(
                        false
                    )
                }
                var selectedIndex by remember { mutableIntStateOf(0) }

                LaunchedEffect(nowDataSource) {
                    nowDataSource?.let { dataSource ->
                        dateBaseList = dataSource.getSchemas(project).apply {
                            dataBases = this
                        }.map {
                            GenData(
                                it.name, AllIconsKeys.Nodes.DataSchema
                            )
                        }.let {
                            isLoad = it.isNotEmpty().apply {
                                if (this) {
                                    // 尝试根据保存的名称恢复选择
                                    val savedDatabaseName = settings.databaseName
                                    val databaseIndex = if (savedDatabaseName.isNotEmpty()) {
                                        dataBases.indexOfFirst { ds -> ds.name == savedDatabaseName }
                                    } else -1

                                    selectedIndex = if (databaseIndex >= 0) databaseIndex else 0
                                    nowDataBase = dataBases[selectedIndex]
                                }
                            }
                            it.ifEmpty {
                                listOf(
                                    GenData(
                                        "", AllIconsKeys.Nodes.DataSchema

                                    )
                                )
                            }
                        }
                    }
                }

                ComboList(dateBaseList, selectedIndex, isLoad) {
                    selectedIndex = it
                    nowDataBase = dataBases[it]
                    saveSettings()
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.select.table").get())
                var tableList by remember {
                    mutableStateOf(
                        listOf(
                            GenData(
                                "", AllIconsKeys.Nodes.DataTables
                            )
                        )
                    )
                }
                var isLoad by remember {
                    mutableStateOf(
                        false
                    )
                }
                var selectedIndex by remember { mutableIntStateOf(0) }
                LaunchedEffect(nowDataBase) {
                    nowDataBase?.let { dataBase ->
                        tableList = dataBase.getTables().apply {
                            tables = this
                        }.map {
                            GenData(it.name, AllIconsKeys.Nodes.DataTables)
                        }.let {
                            isLoad = it.isNotEmpty().apply {
                                if (this) {
                                    // 尝试根据保存的名称恢复选择
                                    val savedTableName = settings.tableName
                                    val tableIndex = if (savedTableName.isNotEmpty()) {
                                        tables.indexOfFirst { ds -> ds.name == savedTableName }
                                    } else -1

                                    selectedIndex = if (tableIndex >= 0) tableIndex else 0
                                    nowTable = tables[selectedIndex]
                                }
                            }
                            it.ifEmpty {
                                listOf(
                                    GenData(
                                        "", AllIconsKeys.Nodes.DataTables
                                    )
                                )
                            }
                        }
                    }
                }

                ComboList(tableList, selectedIndex, isLoad) {
                    selectedIndex = it
                    nowTable = tables[it]
                    saveSettings()
                }


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.select.menu").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = menuState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.cloud.serviceName").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = serviceNameState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.tablePrefix").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = tablePreState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.text.basicBusiness").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(baseMode, first) {

                    baseMode = it
                    saveSettings()
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.tenantModel").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(tenantMode, first) {

                    tenantMode = it
                    saveSettings()
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.useElementUI").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(useElementUI, first) {

                    useElementUI = it
                    saveSettings()
                }


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.text.webPrefix").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = webPreState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.packTheNextLevel").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = code,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.packageTheSecondLevel").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = fucCode,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.text.wrapperMode").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(wrapMode, first) {

                    wrapMode = it
                    saveSettings()
                }
                Spacer(modifier = Modifier.width(16.dp))

                Text(LanguageBundle.messagePointer("tool.gen.text.javaPackageName").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = packageName,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") }
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(LanguageBundle.messagePointer("tool.gen.text.frontEndDirectory").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    enabled = false,
                    state = frontDir,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
                    trailingIcon = {
                        IconButton(onClick = {
                            val descriptor = FileChooserDescriptor(false, true, false, false, false, false)
                            descriptor.title = LanguageBundle.messagePointer("tool.gen.text.frontEndDirectory").get()

                            val virtualFile = FileChooser.chooseFile(descriptor, project, null)
                            virtualFile?.let {
                                frontDir.setTextAndPlaceCursorAtEnd(it.path)
                                saveSettings()
                            }
                        }, modifier = Modifier.pointerHoverIcon(PointerIcon.Hand)) {
                            Icon(AllIconsKeys.Nodes.Folder, contentDescription = null)
                        }


                    })

                Spacer(modifier = Modifier.width(16.dp))


            }

        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            DefaultButton(enabled = buE, onClick = {
                // 在生成代码前保存所有设置
                saveSettings()
                msg = LanguageBundle.messagePointer("tool.gen.compiling").get()
                buE = false
                project.compileAndInvokeSingleModule(devModule?.name ?: "", successCallback = {
                    delay(1000)
                    allModule?.let {
                        for (module in it) {
                            val path = Paths.get(module.moduleFilePath).parent.toString()
                            if (path == nowServicePath || path == nowServiceApiPath) {
                                val roots = ModuleRootManager.getInstance(module).contentRoots
                                VfsUtil.markDirtyAndRefresh(
                                    true, true, true, *roots
                                )
                            }
                        }
                    }
                    msg = ""
                    buE = true
                    project.notifySuccess(LanguageBundle.messagePointer("tool.gen.finish").get())
                }, failCallback = {
                    msg = ""
                    buE = true
                    project.notifyError(LanguageBundle.messagePointer("tool.gen.error").get())
                }

                ) {
                    msg = LanguageBundle.messagePointer("tool.gen.invoke").get()
                    nowDataSource?.let { info ->
                        val obj = it.getDeclaredConstructor().newInstance()
                        val pwd = DatabaseCredentials.getInstance().getCredentials(info).password
                        it.getDeclaredMethod("setDriverName", String::class.java).invoke(obj, info.driverClass)
                        it.getDeclaredMethod("setUrl", String::class.java)
                            .invoke(obj, info.url + "/${nowDataBase?.name}")
                        it.getDeclaredMethod("setUsername", String::class.java).invoke(obj, info.username)
                        it.getDeclaredMethod("setPassword", String::class.java).invoke(obj, pwd.toString())
                        it.getDeclaredMethod("setCodeName", String::class.java).invoke(obj, menuState.text)
                        it.getDeclaredMethod("setServiceName", String::class.java).invoke(obj, serviceNameState.text)
                        it.getDeclaredMethod("setPackageName", String::class.java).invoke(obj, packageName.text)
                        it.getDeclaredMethod("setPackageDir", String::class.java).invoke(obj, nowServicePath)
                        it.getDeclaredMethod("setPackageWebDir", String::class.java).invoke(obj, frontDir.text)
                        it.getDeclaredMethod("setTablePrefix", Array<String>::class.java)
                            .invoke(obj, arrayOf<String>(tablePreState.text.toString()))
                        it.getDeclaredMethod("setIncludeTables", Array<String>::class.java)
                            .invoke(obj, arrayOf<String>(nowTable?.name.toString()))
                        it.getDeclaredMethod("setHasSuperEntity", Boolean::class.javaObjectType).invoke(obj, baseMode)
                        it.getDeclaredMethod("setHasWrapper", Boolean::class.javaObjectType).invoke(obj, wrapMode)
                        it.getDeclaredMethod("setUseElementUI", Boolean::class.javaObjectType).invoke(obj, useElementUI)
                        it.getDeclaredMethod("setPackageApiDir", String::class.java).invoke(obj, nowServiceApiPath)
                        it.getDeclaredMethod("setCode", String::class.java).invoke(obj, code.text)
                        it.getDeclaredMethod("setFucCode", String::class.java).invoke(obj, fucCode.text)
                        it.getDeclaredMethod("setWebPrefix", String::class.java).invoke(obj, webPreState.text)
                        it.getDeclaredMethod("setTenantMode", Boolean::class.javaObjectType).invoke(obj, tenantMode)
                        obj
                    }

                }


            }) { Text(LanguageBundle.messagePointer("tool.sure").get()) }

        }


    }
    if (!buE) {
        Box(
            modifier = Modifier.fillMaxSize().background(color = Color.Black.copy(alpha = 0.3f)).clickable(
                indication = null, interactionSource = remember { MutableInteractionSource() }) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicatorBig()
                Spacer(modifier = Modifier.height(6.dp))
                Text(msg)
            }
        }
    }
}

@Composable
fun textFieldState(): TextFieldState = remember { TextFieldState("") }

@OptIn(ExperimentalJewelApi::class)
@Composable
fun ComboList(
    data: List<GenData>, selectedIndex: Int, dataSourceIsLoad: Boolean, indexChange: (Int) -> Unit
) {
    Spacer(modifier = Modifier.width(8.dp))
    ListComboBox(
        items = data,
        selectedIndex = selectedIndex,
        modifier = Modifier.widthIn(max = 400.dp),
        onSelectedItemChange = { index -> indexChange(index) },
        itemKeys = { index, _ -> index },
        enabled = dataSourceIsLoad,
        itemContent = { item, isSelected, isActive ->
            SimpleListItem(
                text = item.name,
                selected = isSelected,
                active = isActive,
                iconContentDescription = item.name,
                icon = item.icon,
                colorFilter = null,
            )
        },
    )
}

@Composable
private fun CheckListTrue(
    initValue: Boolean,
    first: Boolean,
    callback: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        var index by remember(initValue) { mutableIntStateOf(if (initValue) 1 else 0) }
        LaunchedEffect(index, first) {
            if (!first) {
                callback(index == 1)
            }
        }
        RadioButtonRow(selected = index == 0, onClick = { index = 0 }) {
            Text(LanguageBundle.messagePointer("tool.no").get())
        }
        RadioButtonRow(
            selected = index == 1, onClick = { index = 1 }) { Text(LanguageBundle.messagePointer("tool.yes").get()) }
    }
}

data class GenData(val name: String, val icon: IconKey, val other: String = "", val module: Module? = null)
