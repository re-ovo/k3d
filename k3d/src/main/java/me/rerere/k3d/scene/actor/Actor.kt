@file:Suppress("MemberVisibilityCanBePrivate")

package me.rerere.k3d.scene.actor

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import me.rerere.k3d.util.math.transform.applyRotation
import me.rerere.k3d.util.math.transform.applyScale
import me.rerere.k3d.util.math.transform.applyTranslation
import me.rerere.k3d.util.math.transform.scaleMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import me.rerere.k3d.util.system.Dirty
import java.util.UUID

abstract class Actor : Dirty {
    private val _id = UUID.randomUUID()
    var name: String = ""

    var parent: Actor? = null

    val position = Vec3(0f, 0f, 0f)
    val rotation = Quaternion(0f, 0f, 0f, 1f)
    val scale = Vec3(1f, 1f, 1f)

    override fun updateDirty() {
        if(this.rotation.isDirty()) this.rotation.updateDirty()

        this.updateMatrix()
    }

    override fun isDirty(): Boolean {
        return position.isDirty() || rotation.isDirty() || scale.isDirty()
    }

    override fun clearDirty() {
        position.clearDirty()
        rotation.clearDirty()
        scale.clearDirty()
    }

    override fun markDirtyNew() {
        error("Actor is not a dirty value")
    }

    private var _localMatrix = Matrix4.identity()
    val localMatrix: Matrix4
        get() = _localMatrix

    private var _worldMatrix = Matrix4.identity()
    val worldMatrix: Matrix4
        get() = _worldMatrix

    open fun updateMatrix() {
        _localMatrix.setToIdentity()
            .applyTranslation(position.x, position.y, position.z)
            .applyRotation(rotation)
            .applyScale(scale.x, scale.y, scale.z)

        _worldMatrix = parent?.let {
            _worldMatrix.multiplyMatrices(it.worldMatrix, _localMatrix)
        } ?: localMatrix
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