package me.rerere.k3d.renderer

import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.Camera

interface Renderer {
    val viewportSize: ViewportSize

    fun render(scene: Scene, camera: Camera)

    fun resize(width: Int, height: Int)
}

data class ViewportSize(
    val width: Int,
    val height: Int
)