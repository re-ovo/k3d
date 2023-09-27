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

    private val MIN_PITCH = -Math.PI.toFloat() / 2
    private val MAX_PITCH = Math.PI.toFloat() / 2

    private fun handleDrag(rotate: ControllerEvent.Rotate) {
        val dx = rotate.deltaX / element.height.toFloat()
        val dy = rotate.deltaY / element.height.toFloat()

        val newYaw = camera.yaw - dx * 5f
        val newPitch = (camera.pitch - dy * 5f).coerceIn(MIN_PITCH, MAX_PITCH)

        update(newYaw, newPitch, distance)
    }

    private fun handleZoom(event: ControllerEvent.Zoom) {
        val delta = -event.delta
        val dist = this.distance + delta / 50f

        update(camera.yaw, camera.pitch, dist.coerceIn(0.1f, 100f))
    }

    private fun update(newYaw: Float, newPitch: Float, newDistance: Float) {
        val x = target.x + newDistance * cos(newPitch) * sin(newYaw)
        val y = target.y + newDistance * sin(-newPitch)
        val z = target.z + newDistance * cos(newPitch) * cos(newYaw)
        this.distance = newDistance
        this.camera.position.set(x, y, z)
        this.camera.yaw = newYaw
        this.camera.pitch = newPitch
    }
}