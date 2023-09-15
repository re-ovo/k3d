package me.rerere.k3d.scene.camera

import me.rerere.k3d.scene.Actor
import me.rerere.k3d.util.math.Matrix4

abstract class Camera : Actor() {
    val worldMatrixInverse = Matrix4.identity()
    abstract val projectionMatrix: Matrix4

    override fun updateMatrix() {
        super.updateMatrix()
        worldMatrixInverse.setData(worldMatrix.inverse().data)
    }

    abstract fun updateProjectionMatrix()
}