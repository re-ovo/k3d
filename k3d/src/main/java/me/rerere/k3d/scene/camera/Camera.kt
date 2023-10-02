package me.rerere.k3d.scene.camera

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Euler
import me.rerere.k3d.util.math.transform.rotationMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import me.rerere.k3d.util.system.dirtyValue
import me.rerere.k3d.util.system.markDirty
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

private const val MIN_PITCH = -Math.PI.toFloat() / 2
private const val MAX_PITCH = Math.PI.toFloat() / 2

abstract class Camera : Actor() {
    var yaw: Float by dirtyValue(0f)
    var pitch: Float by dirtyValue(
        initialValue = 0f,
        setter = { _, newValue ->
            newValue.coerceIn(MIN_PITCH, MAX_PITCH)
        }
    )
    var roll: Float by dirtyValue(0f)

    val worldMatrixInverse = Matrix4.identity()
    abstract val projectionMatrix: Matrix4

    override fun updateMatrix() {
        worldMatrix.set(
            translationMatrix(
                position.x,
                position.y,
                position.z
            ) * rotationMatrix(pitch, yaw, roll)
        )
        // rotation.set(Euler(pitch, yaw, roll).toQuaternion())
        worldMatrixInverse.set(worldMatrix.inverse())
    }

    fun lookAt(target: Vec3) { // default target to -Z
        val direction = (target - position).normalizeSelf()

        pitch = if (direction.x == 0f && direction.z == 0f) {
            if (direction.y > 0) {
                -Math.PI.toFloat() / 2
            } else {
                Math.PI.toFloat() / 2
            }
        } else {
            asin(direction.y)
        }

        if (abs(direction.x) > 1e-2 || abs(direction.z) > 1e-2) {
            yaw = atan2(-direction.x, -direction.z)
        }
    }

    abstract fun updateProjectionMatrix()
}