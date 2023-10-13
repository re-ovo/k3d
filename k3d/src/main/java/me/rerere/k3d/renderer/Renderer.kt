package me.rerere.k3d.renderer

import me.rerere.k3d.scene.actor.Scene
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.util.system.Disposable

interface Renderer : Disposable {
    val viewportSize: ViewportSize

    fun render(scene: Scene, camera: Camera)

    fun resize(width: Int, height: Int)

    /**
     * Run a task on render thread
     *
     * This can avoid resource out-of-sync problems caused by updating scene trees in different threads.
     * For example, if you want to dispose a mesh, if you run it in other threads, it may cause the GPU
     * resource to be released before the render process is completed, resulting in a crash.
     *
     * @param block the task
     */
    fun runOnRenderThread(block: () -> Unit)
}

data class ViewportSize(
    val width: Int,
    val height: Int
)