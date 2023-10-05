package me.rerere.k3d.scene.camera

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.transform.rotationMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import me.rerere.k3d.util.system.dirtyFloatValue
import me.rerere.k3d.util.system.dirtyValue
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

private const val MIN_PITCH = -Math.PI.toFloat() / 2
private const val MAX_PITCH = Math.PI.toFloat() / 2

abstract class Camera : Actor() {
    private var _dirty = false

    var yaw: Float by dirtyFloatValue(0f)
    var pitch: Float by dirtyFloatValue(
        initialValue = 0f,
        setter = { _, newValue ->
            newValue.coerceIn(MIN_PITCH, MAX_PITCH)
        }
    )
    var roll: Float by dirtyFloatValue(0f)

    val worldMatrixInverse = Matrix4.identity()
    abstract val projectionMatrix: Matrix4

    override fun isDirty(): Boolean {
        return _dirty
    }

    override fun markDirtyNew() {
        _dirty = true
    }

    override fun clearDirty() {
        _dirty = false
    }

    override fun updateMatrix() {
        worldMatrix.set(
            translationMatrix(
                position.x,
                position.y,
                position.z
            ) * rotationMatrix(pitch, yaw, roll)
        )
        worldMatrixInverse.set(worldMatrix.inverse())
    }

    override fun updateDirty() {
        updateMatrix()
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