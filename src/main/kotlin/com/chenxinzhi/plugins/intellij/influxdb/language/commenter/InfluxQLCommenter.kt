package com.chenxinzhi.plugins.intellij.influxdb.language.commenter


import com.intellij.lang.Commenter

class InfluxQLCommenter : Commenter {

    /**
     * 返回单行注释的前缀。
     * 对于 InfluxQL，就是 "--"。
     */
    override fun getLineCommentPrefix(): String? {
        return "--"
    }

    /**
     * 返回块注释的开始符号。
     * InfluxQL 没有块注释，所以我们返回 null。
     */
    override fun getBlockCommentPrefix(): String? {
        return null
    }

    /**
     * 返回块注释的结束符号。
     * InfluxQL 没有块注释，所以我们返回 null。
     */
    override fun getBlockCommentSuffix(): String? {
        return null
    }

    /**
     * 当取消注释时，要查找的注释前缀。
     * 通常和 getLineCommentPrefix() 返回的一样。
     */
    override fun getCommentedBlockCommentPrefix(): String? {
        return lineCommentPrefix
    }

    /**
     * 当取消注释时，要查找的注释后缀。
     */
    override fun getCommentedBlockCommentSuffix(): String? {
        return null
    }
}