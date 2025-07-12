package com.chenxinzhi.plugins.intellij.services;



/**
 * GenCode 项目设置数据模型
 * 用于持久化存储 GenCode 界面的所有状态
 *
 * @author chenxinzhi
 * @date 2025-01-27
 */
data class GenCodeProjectSettings(
    // 数据源相关
    var dataSourceName: String = "",
    var databaseName: String = "",
    var tableName: String = "",
    
    // 模块相关
    var devModuleName: String = "",
    var servicePath: String = "",
    var serviceApiPath: String = "",
    
    // 文本输入字段
    var menuText: String = "",
    var serviceNameText: String = "",
    var tablePrefixText: String = "",
    var webPrefixText: String = "",
    var codeText: String = "",
    var fucCodeText: String = "",
    var packageNameText: String = "",
    var frontDirText: String = "",
    
    // 布尔选项
    var baseMode: Boolean = false,
    var tenantMode: Boolean = false,
    var useElementUI: Boolean = false,
    var wrapMode: Boolean = false
)