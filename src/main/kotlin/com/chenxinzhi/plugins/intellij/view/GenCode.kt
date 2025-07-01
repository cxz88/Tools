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
import kotlin.math.max

/**
 * @author chenxinzhi
 * @date 2025-06-27 09:21:25
 */
var dataSources: List<LocalDataSource> by mutableStateOf(listOf())
var devModules: List<Module> by mutableStateOf(listOf())
var dataBases: List<DasNamespace> by mutableStateOf(listOf())
var nowDataSource: LocalDataSource? by mutableStateOf(null)
var nowDataBase: DasNamespace? by mutableStateOf(null)
var nowServicePath: String? by mutableStateOf(null)
var nowServiceApiPath: String? by mutableStateOf(null)
var tables: List<DasObject> by mutableStateOf(listOf())
var nowTable: DasObject? by mutableStateOf(null)
val menuState = TextFieldState("")
val serviceNameState = TextFieldState("")
val tablePreState = TextFieldState("")
var baseMode by
mutableStateOf(false)

var tenantMode by
mutableStateOf(false)

var useElementUI by
mutableStateOf(false)

val webPreState = TextFieldState("")
val code = TextFieldState("")
val fucCode = TextFieldState("")
val packageName = TextFieldState("")
val frontDir = TextFieldState("")
var wrapMode by
mutableStateOf(false)

var devModule: Module? by
mutableStateOf(null)

var allModule: List<Module>? by
mutableStateOf(null)

@Composable
fun GenCode(project: Project) {
    var buE by remember { mutableStateOf(true) }
    var msg by remember { mutableStateOf("") }
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
                        devModule = devModules[devModule?.let { module ->
                            devModules.indexOfFirst { m -> m.name == module.name }.let { i ->
                                max(i, 0).apply {
                                    selectedIndex = this
                                }
                            }
                        } ?: 0]
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
                            nowDataSource = dataSources[nowDataSource?.let { lds ->
                                dataSources.indexOfFirst { ds -> ds.name == lds.name }.let { i ->
                                    max(i, 0).apply {
                                        selectedIndex = this
                                    }
                                }
                            } ?: 0]
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
                            nowServicePath = it[nowServicePath?.let { module ->
                                it.indexOfFirst { m -> m.other == module }.let { i ->
                                    max(i, 0).apply {
                                        selectedIndex = this
                                    }
                                }
                            } ?: 0].other

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
                            nowServiceApiPath = it[nowServiceApiPath?.let { module ->
                                it.indexOfFirst { m -> m.other == module }.let { i ->
                                    max(i, 0).apply {
                                        selectedIndex = this
                                    }
                                }
                            } ?: 0].other

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
                                    nowDataBase = dataBases[nowDataBase?.let { lds ->
                                        dataBases.indexOfFirst { ds -> ds.name == lds.name }.let { i ->
                                            max(i, 0).apply {
                                                selectedIndex = this
                                            }
                                        }
                                    } ?: 0]

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
                                    nowTable = tables[nowTable?.let { lds ->
                                        tables.indexOfFirst { ds -> ds.name == lds.name }.let { i ->
                                            max(i, 0).apply {
                                                selectedIndex = this
                                            }
                                        }
                                    } ?: 0]

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
                    placeholder = { Text("") },
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.cloud.serviceName").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = serviceNameState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.tablePrefix").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = tablePreState,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
                )


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.text.basicBusiness").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(baseMode) {
                    baseMode = it
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.tenantModel").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(tenantMode) {
                    tenantMode = it
                }


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.useElementUI").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(useElementUI) {
                    useElementUI = it
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
                    placeholder = { Text("") },
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.packTheNextLevel").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = code,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
                )


            }
            Spacer(modifier = Modifier.width(16.dp))
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(LanguageBundle.messagePointer("tool.gen.text.packageTheSecondLevel").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = fucCode,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
                )


            }

        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Row(horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {

                Text(LanguageBundle.messagePointer("tool.gen.text.wrapperMode").get())
                Spacer(Modifier.width(8.dp))
                CheckListTrue(wrapMode) {
                    wrapMode = it
                }
                Spacer(modifier = Modifier.width(16.dp))

                Text(LanguageBundle.messagePointer("tool.gen.text.javaPackageName").get())
                Spacer(Modifier.width(8.dp))
                TextField(
                    state = packageName,
                    modifier = Modifier.width(400.dp),
                    placeholder = { Text("") },
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

@OptIn(ExperimentalJewelApi::class)
@Composable
private fun ComboList(
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
    callback: (Boolean) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        var index by remember { mutableIntStateOf(if (initValue) 1 else 0) }
        LaunchedEffect(index) {
            callback(index == 1)
        }
        RadioButtonRow(selected = index == 0, onClick = { index = 0 }) {
            Text(LanguageBundle.messagePointer("tool.no").get())
        }
        RadioButtonRow(
            selected = index == 1, onClick = { index = 1 }) { Text(LanguageBundle.messagePointer("tool.yes").get()) }
    }
}

data class GenData(val name: String, val icon: IconKey, val other: String = "")
