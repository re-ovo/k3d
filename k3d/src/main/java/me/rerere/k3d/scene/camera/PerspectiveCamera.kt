package me.rerere.k3d.scene.camera

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.rotation.toRadian
import me.rerere.k3d.util.math.transform.perspectiveMatrix
import kotlin.math.tan

class PerspectiveCamera(
    fov: Float = 60f,
    aspect: Float = 1f,
    near: Float = 0.1f,
    far: Float = 100f
): Camera() {
    var fov = fov
        set(value) {
            field = value
            updateProjectionMatrix()
        }
    var aspect = aspect
        set(value) {
            field = value
            updateProjectionMatrix()
        }
    var near = near
        set(value) {
            field = value
            updateProjectionMatrix()
        }
    var far = far
        set(value) {
            field = value
            updateProjectionMatrix()
        }

    override val projectionMatrix: Matrix4 = Matrix4.identity()

    override fun updateProjectionMatrix() {
        val fovRad = fov.toRadian()
        val top = near * tan(fovRad / 2)
        val height = 2 * top
        val width = height * aspect
        val left = -width / 2

        projectionMatrix.set(perspectiveMatrix(
            left = left,
            right = left + width,
            top = top,
            bottom = top - height,
            near = near,
            far = far
        ).data)
    }

    init {
        updateMatrix()
        updateProjectionMatrix()
    }
}