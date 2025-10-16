package com.chenxinzhi.plugins.intellij.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "TranService",
    storages = [Storage("tran-settings.xml")]
)
class TranService(private val project: Project) :
    PersistentStateComponent<Tran> {

    private var state = Tran()

    override fun getState(): Tran = state

    override fun loadState(state: Tran) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): TranService {
            return project.getService(TranService::class.java)
        }
    }
}