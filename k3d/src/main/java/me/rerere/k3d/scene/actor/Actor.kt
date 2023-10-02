@file:Suppress("MemberVisibilityCanBePrivate")

package me.rerere.k3d.scene.actor

import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import me.rerere.k3d.util.math.transform.scaleMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import java.util.UUID

abstract class Actor : Dirty {
    private val _id = UUID.randomUUID()
    override var dirty: Boolean = false
        get() = field || position.dirty || rotation.dirty || scale.dirty
        set(value) {
            field = value
            position.dirty = value
            rotation.dirty = value
            scale.dirty = value
        }
    var name: String = ""

    var parent: Actor? = null
    val position = Vec3(0f, 0f, 0f)
    val rotation = Quaternion(0f, 0f, 0f, 1f)
    val scale = Vec3(1f, 1f, 1f)

    private var _localMatrix = Matrix4.identity()
    val localMatrix: Matrix4
        get() = _localMatrix

    private var _worldMatrix = Matrix4.identity()
    val worldMatrix: Matrix4
        get() = _worldMatrix

    open fun updateMatrix() {
        _localMatrix = scaleMatrix(scale).applyMatrix4(rotation.toMatrix4())
            .applyMatrix4(translationMatrix(position))
        _worldMatrix = parent?.worldMatrix?.times(_localMatrix) ?: _localMatrix

        if (this is ActorGroup) {
            getChildren().forEach {
                it.updateMatrix()
            }
        }
    }

    /**
     * The tick function will be called every frame
     *
     * Note that the tick function was called every frame, so it's not recommended to do heavy work
     * in this function, also you need to handle the deltaTime, because the frame rate may be different
     * on different devices and different scenes
     *
     * @param deltaTime the time between two frames
     *
     */
    open fun tick(deltaTime: Float) {}

    override fun hashCode(): Int {
        return _id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Actor) return false
        return _id == other._id
    }

    override fun toString(): String {
        return "${javaClass.simpleName}(id=$_id, name='$name', parent=${parent?.name}, position=$position, rotation=$rotation, scale=$scale)"
    }
}