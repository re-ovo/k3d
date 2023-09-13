package me.rerere.k3d.util

interface K3DLogger {
    fun log(message: String)
    fun warn(message: String)
    fun error(message: String)
    fun debug(message: String)
}

object K3DLoggerImpl : K3DLogger {
    override fun log(message: String) {
        println("[K3D] $message")
    }

    override fun warn(message: String) {
        println("[K3D] [WARN] $message")
    }

    override fun error(message: String) {
        println("[K3D] [ERROR] $message")
    }

    override fun debug(message: String) {
        println("[K3D] [DEBUG] $message")
    }
}