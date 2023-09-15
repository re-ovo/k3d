package me.rerere.k3d.scene.camera

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.rotation.toRadian
import me.rerere.k3d.util.math.transform.perspectiveMatrix
import kotlin.math.tan

class PerspectiveCamera(
    private var fov: Float = 60f,
    private var aspect: Float = 1f,
    private var near: Float = 0.1f,
    private var far: Float = 1000f
): Camera() {
    override val projectionMatrix: Matrix4 = Matrix4.identity()

    override fun updateProjectionMatrix() {
        val fovRad = fov.toRadian()
        val top = near * tan(fovRad / 2)
        val height = 2 * top
        val width = height * aspect
        val left = -width / 2

        projectionMatrix.setData(perspectiveMatrix(
            left = left,
            right = left + width,
            top = top,
            bottom = top - height,
            near = near,
            far = far
        ).data)
    }

    init {
        updateProjectionMatrix()
    }
}