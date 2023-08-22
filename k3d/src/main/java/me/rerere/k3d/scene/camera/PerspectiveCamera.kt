package me.rerere.k3d.scene.camera

import me.rerere.k3d.util.math.Matrix4

class PerspectiveCamera(
    private var fov: Float = 60f,
    private var aspect: Float = 1f,
    private var near: Float = 0.1f,
    private var far: Float = 1000f
): Camera() {
    override fun getProjectionMatrix(): Matrix4 {
        TODO("Not yet implemented")
    }

    override fun getViewMatrix(): Matrix4 {
        TODO("Not yet implemented")
    }
}