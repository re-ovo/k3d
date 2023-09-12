package me.rerere.k3d.scene.camera

import me.rerere.k3d.scene.Actor
import me.rerere.k3d.util.math.Matrix4

abstract class Camera : Actor() {
    abstract fun getProjectionMatrix(): Matrix4

    abstract fun getViewMatrix(): Matrix4
}

object DummyCamera : Camera() {
    override fun getProjectionMatrix(): Matrix4 {
        return Matrix4.identity()
    }

    override fun getViewMatrix(): Matrix4 {
        return Matrix4.identity()
    }
}