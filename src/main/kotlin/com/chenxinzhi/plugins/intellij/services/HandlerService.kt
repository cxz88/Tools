package com.chenxinzhi.plugins.intellij.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * @author chenxinzhi
 * @date 2025-06-20 19:55:59
 */
@Service(Service.Level.PROJECT)
class HandlerService(
    private val project: Project, private val cs: CoroutineScope
) {
    fun handler(block: suspend () -> Unit) {
        cs.launch {
            block()
        }
    }

}