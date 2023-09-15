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

    fun rotateX(angle: Float) {
        println("rotateX: $angle")
        val euler = this.rotation.toEuler()
        euler.x += angle
        euler.x %= (2 * Math.PI.toFloat())
        this.rotation.set(euler.toQuaternion())
    }

    fun rotateY(angle: Float) {
        println("rotateY: $angle")
        val euler = this.rotation.toEuler()
        euler.y += angle
        euler.y %= (2 * Math.PI.toFloat())
        this.rotation.set(euler.toQuaternion())
    }

    abstract fun updateProjectionMatrix()
}