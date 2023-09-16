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

    private var lastX = 0f
    private var lastY = 0f
    private var lastDistance = 0f

    fun handleEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.x
                lastY = event.y
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                lastDistance = event.getPointerDistance(0, 1)
            }
            MotionEvent.ACTION_MOVE -> {
                when(event.pointerCount) {
                    1 -> {
                        handleDrag(event)
                    }

                    2 -> {
                        handleZoom(event)
                    }
                }

                lastX = event.x
                lastY = event.y
            }
        }
    }

    private fun handleDrag(event: MotionEvent) {
        val dx = (event.x - lastX) / element.height.toFloat()
        val dy = (event.y - lastY) / element.height.toFloat()

        println("Drag: $dx, $dy")

        camera.yaw -= dx * 5f
        camera.pitch -= dy * 5f
        camera.pitch = camera.pitch.coerceIn(-90f, 90f)

        update()
    }

    private fun handleZoom(event: MotionEvent) {
        val distance = event.getPointerDistance(0, 1)
        val delta = -(distance - lastDistance)
        lastDistance = distance

        println("Zoom: $delta")
        this.distance += delta / 50f
        this.distance = this.distance.coerceIn(0.1f, 100f)

        update()
    }

    fun update() {
        val x = target.x + distance * cos(camera.pitch) * sin(camera.yaw)
        val y = target.y + distance * sin(-camera.pitch)
        val z = target.z + distance * cos(camera.pitch) * cos(camera.yaw)
        camera.position.set(x, y, z)
        camera.lookAt(target)
    }
}