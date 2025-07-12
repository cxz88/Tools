package com.chenxinzhi.plugins.intellij.services

import com.chenxinzhi.plugins.intellij.model.InfluxDbProjectSettings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
@State(
    name = "InfluxDbProjectSettingsService",
    storages = [Storage("influxdb-settings.xml")]
)
class InfluxDbProjectSettingsService(private val project: Project) :
    PersistentStateComponent<InfluxDbProjectSettings> {

    private var state = InfluxDbProjectSettings()

    override fun getState(): InfluxDbProjectSettings = state

    override fun loadState(state: InfluxDbProjectSettings) {
        this.state = state
    }

    companion object {
        fun getInstance(project: Project): InfluxDbProjectSettingsService {
            return project.getService(InfluxDbProjectSettingsService::class.java)
        }
    }
}