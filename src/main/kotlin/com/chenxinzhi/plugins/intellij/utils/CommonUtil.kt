package com.chenxinzhi.plugins.intellij.utils

import com.chenxinzhi.plugins.intellij.services.HandlerService
import com.intellij.database.dataSource.LocalDataSource
import com.intellij.database.dataSource.LocalDataSourceManager
import com.intellij.database.dataSource.isLoading
import com.intellij.database.model.DasNamespace
import com.intellij.database.model.ObjectKind
import com.intellij.database.psi.DataSourceManager
import com.intellij.database.util.DasUtil
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileStatusNotification
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.components.service
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderEnumerator
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtilCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.jetbrains.jps.model.java.JavaResourceRootType
import java.io.File
import java.net.URL
import java.net.URLClassLoader


fun Project.compileAndInvokeSingleModule(
    moduleName: String,
    className: String = "org.springblade.develop.support.BladeCodeGenerator",
    methodName: String = "run",
    methodArgs: Array<Any> = emptyArray(),
    methodArgTypes: Array<Class<*>> = emptyArray(),
    successCallback: suspend () -> Unit = {},
    failCallback: () -> Unit = {},
    setObj: (Class<*>) -> Any?
) {
    val module = ModuleManager.getInstance(this).findModuleByName(moduleName) ?: run {
        return
    }

    CompilerManager.getInstance(this).make(module, object : CompileStatusNotification {
        override fun finished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
            try {
                if (aborted || errors > 0) {
                    return
                }
                val urls = mutableListOf<URL>()
                val outputPath = CompilerPaths.getModuleOutputPath(module, false) ?: run {
                    return
                }
                OrderEnumerator.orderEntries(module).recursively().classes().roots.forEach { virtualFile ->
                    val url = when {
                        virtualFile.fileSystem.protocol == JarFileSystem.PROTOCOL -> {
                            // 是 jar 包中的 VirtualFile，获取原始 jar 文件路径
                            val jarFile = JarFileSystem.getInstance().getVirtualFileForJar(virtualFile)
                            jarFile?.let { VfsUtilCore.virtualToIoFile(it).toURI().toURL() }
                        }

                        else -> {
                            // 普通目录、文件等
                            VfsUtilCore.virtualToIoFile(virtualFile).toURI().toURL()
                        }
                    }
                    url?.let { urls += it }
                }
                urls += File(outputPath).toURI().toURL()
                val resourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(JavaResourceRootType.RESOURCE)
                resourceRoots.forEach { vf ->
                    urls += VfsUtilCore.virtualToIoFile(vf).toURI().toURL()
                }
                val service = service<HandlerService>()
                service.handler {
                    withContext(Dispatchers.IO) {
                        URLClassLoader(urls.toTypedArray(), javaClass.classLoader).use { classLoader ->
                            val orig = Thread.currentThread().contextClassLoader
                            try {
                                Thread.currentThread().contextClassLoader = classLoader
                                val clazz = classLoader.loadClass(className)
                                Class.forName("com.mysql.cj.jdbc.Driver", true, classLoader)
                                val obj = setObj(clazz)
                                obj?.let {
                                    val method = clazz.getMethod(methodName, *methodArgTypes)
                                    method.invoke(it, *methodArgs)
                                }

                            } finally {
                                Thread.currentThread().contextClassLoader = orig
                            }

                        }
                    }
                    successCallback()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                failCallback()
            }


        }
    })
}

fun Project.notifySuccess(message: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("CxzGroup")
        .createNotification(message, NotificationType.INFORMATION).notify(this)
}

fun Project.notifyError(message: String) {
    NotificationGroupManager.getInstance().getNotificationGroup("CxzGroup")
        .createNotification(message, NotificationType.ERROR).notify(this)
}

suspend fun LocalDataSource.getSchemas(project: Project) = coroutineScope {
    suspend {
        while (dataSource.isLoading(project)) {
            delay(1000)
        }
        DasUtil.getSchemas(dataSource).toList()
    }().filterNot {
        it.name in listOf(
            "information_schema", "mysql", "performance_schema", "sys"
        )
    }
}

fun DasNamespace.getTables() = getDasChildren(ObjectKind.TABLE).toList().toList()

fun Project.getAllDataSources(): List<LocalDataSource> {
    val managers = DataSourceManager.getManagers(this)
    return managers.filterIsInstance<LocalDataSourceManager>().first().dataSources
}

fun Project.getAllModules(): List<Module> {
    return ModuleManager.getInstance(this).modules.toList()
}
