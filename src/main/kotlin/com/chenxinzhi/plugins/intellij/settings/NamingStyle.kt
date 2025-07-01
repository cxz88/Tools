/**
 * 命名风格数据类
 */
data class NamingStyle(
    var name: String, // 显示名称
    var order: Int,   // 顺序
    var methodName: String, // 对应的方法名
    var useScript: Boolean = true, // 是否使用脚本，默认为true
    var scriptContent: String = "" // 脚本内容
)
