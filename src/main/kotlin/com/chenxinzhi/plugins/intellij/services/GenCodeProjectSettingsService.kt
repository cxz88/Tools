package com.chenxinzhi.plugins.intellij.services

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "GenCodeProjectSettingsService",
    storages = [Storage(StoragePathMacros.WORKSPACE_FILE)]
)
class GenCodeProjectSettingsService(private val project: Project) :
    PersistentStateComponent<GenCodeProjectSettings> {

    private var state = GenCodeProjectSettings()

    override fun getState(): GenCodeProjectSettings = state

    override fun loadState(state: GenCodeProjectSettings) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): GenCodeProjectSettingsService {
            return project.getService(GenCodeProjectSettingsService::class.java)
        }
    }
}