package com.chenxinzhi.plugins.intellij.services

import com.intellij.openapi.components.*
import com.intellij.openapi.project.Project

/**
 * 二维码设置持久化服务
 * @author chenxinzhi
 * @date 2025-12-04
 */
@Service(Service.Level.PROJECT)
@State(name = "QRCodeSettings", storages = [Storage("QRCodeSettings.xml")])
class QRCodeSettingsService : PersistentStateComponent<QRCodeSettingsService.State> {
    
    private var myState = State()
    
    data class State(
        var text: String = "",
        var size: Int = 300
    )
    
    override fun getState(): State = myState
    
    override fun loadState(state: State) {
        myState = state
    }
    
    companion object {
        fun getInstance(project: Project): QRCodeSettingsService {
            return project.getService(QRCodeSettingsService::class.java)
        }
    }
}
