package me.rerere.k3d.scene.camera

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.Dirty
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Euler
import me.rerere.k3d.util.math.transform.rotationMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

private const val MIN_PITCH = -Math.PI.toFloat() / 2
private const val MAX_PITCH = Math.PI.toFloat() / 2

abstract class Camera : Actor(), Dirty {
    var yaw: Float = 0f
        set(value) {
            field = value
            markDirty()
        }
    var pitch: Float = 0f
        set(value) {
            field = value.coerceIn(MIN_PITCH, MAX_PITCH)
            markDirty()
        }
    var roll: Float = 0f
        set(value) {
            field = value
            markDirty()
        }
    override var dirty: Boolean = false
        get() {
            return field || super.dirty
        }
        set(value) {
            field = value
            super.dirty = value
        }

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
        rotation.set(Euler(pitch, yaw, roll).toQuaternion())
        // super.updateMatrix()
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

        if(abs(direction.x) > 1e-2 || abs(direction.z) > 1e-2) {
            yaw = atan2(-direction.x, -direction.z)
        }
    }

    abstract fun updateProjectionMatrix()
}