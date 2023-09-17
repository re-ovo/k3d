package me.rerere.k3d.controller

import android.view.MotionEvent
import android.view.View
import me.rerere.k3d.scene.camera.Camera
import me.rerere.k3d.util.math.Vec3
import kotlin.math.cos
import kotlin.math.sin

/**
 * Orbit controller
 *
 * @param camera The camera to control
 * @param target The target to orbit around
 */
class OrbitController(
    val camera: Camera,
    val target: Vec3,
    val element: View,
) {
    private var distance = 5f
    private val handler = MotionHandler { event ->
        println(event)

        when (event) {
            is ControllerEvent.Rotate -> {
                handleDrag(event)
            }

            is ControllerEvent.Zoom -> {
                handleZoom(event)
            }

            is ControllerEvent.Pan -> {
                // handlePan(event)
            }
        }
    }

    fun handleEvent(event: MotionEvent) {
        handler.handle(event)
    }

    private fun handleDrag(rotate: ControllerEvent.Rotate) {
        val dx = rotate.deltaX / element.height.toFloat()
        val dy = rotate.deltaY / element.height.toFloat()

        camera.yaw -= dx * 5f
        camera.pitch -= dy * 5f
        camera.pitch = camera.pitch.coerceIn(-90f, 90f)

        update()
    }

    private fun handleZoom(event: ControllerEvent.Zoom) {
        val delta = -event.delta

        println("Zoom: $delta")
        this.distance += delta / 50f
        this.distance = this.distance.coerceIn(0.1f, 100f)

        update()
    }

    private fun update() {
        val x = target.x + distance * cos(camera.pitch) * sin(camera.yaw)
        val y = target.y + distance * sin(-camera.pitch)
        val z = target.z + distance * cos(camera.pitch) * cos(camera.yaw)
        camera.position.set(x, y, z)
        camera.lookAt(target)
    }
}