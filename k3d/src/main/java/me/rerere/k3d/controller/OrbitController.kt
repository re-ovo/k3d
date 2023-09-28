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
    var distanceRange: ClosedFloatingPointRange<Float> = 0.1f..100f

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

        val newYaw = camera.yaw - dx * 5f
        val newPitch = camera.pitch - dy * 5f
        camera.yaw = newYaw
        camera.pitch = newPitch

        // 根据yaw和pitch计算新的位置
        val dist = camera.position.distance(target)
        val x = target.x + dist * cos(camera.pitch) * sin(camera.yaw)
        val y = target.y + dist * sin(-camera.pitch)
        val z = target.z + dist * cos(camera.pitch) * cos(camera.yaw)
        camera.position.set(x, y, z)

        update()
    }

    private fun handleZoom(event: ControllerEvent.Zoom) {
        val delta = -event.delta
        val oldDist = camera.position.distance(target)
        val newDist = (oldDist + delta / 50f)

        val x = target.x + newDist * cos(camera.pitch) * sin(camera.yaw)
        val y = target.y + newDist * sin(-camera.pitch)
        val z = target.z + newDist * cos(camera.pitch) * cos(camera.yaw)
        camera.position.set(x, y, z)

        update()
    }

    private fun update() {
        // 限制距离
        val dist = camera.position.distance(target)
        if (dist !in distanceRange) {
            val newDist = dist.coerceIn(distanceRange)
            val x = target.x + newDist * cos(camera.pitch) * sin(camera.yaw)
            val y = target.y + newDist * sin(-camera.pitch)
            val z = target.z + newDist * cos(camera.pitch) * cos(camera.yaw)
            camera.position.set(x, y, z)
        }

        // 更新相机朝向
        camera.lookAt(target)
    }
}