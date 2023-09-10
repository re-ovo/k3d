package me.rerere.k3d.renderer

import me.rerere.k3d.scene.Scene
import me.rerere.k3d.scene.camera.Camera

class GL3Renderer  : Renderer {
    override var viewportSize: ViewportSize = ViewportSize(0, 0)

    override fun resize(width: Int, height: Int) {
        viewportSize = ViewportSize(width, height)
    }

    override fun render(scene: Scene, camera: Camera) {
        TODO("Not yet implemented")
    }
}