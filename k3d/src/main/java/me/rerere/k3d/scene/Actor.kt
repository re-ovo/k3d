@file:Suppress("MemberVisibilityCanBePrivate")

package me.rerere.k3d.scene

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import me.rerere.k3d.util.math.transform.rotationMatrix
import me.rerere.k3d.util.math.transform.scaleMatrix
import me.rerere.k3d.util.math.transform.translationMatrix
import java.util.UUID

abstract class Actor {
    private val _id = UUID.randomUUID()

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

    open fun tick(deltaTime: Float) {}

    override fun hashCode(): Int {
        return _id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Actor) return false
        return _id == other._id
    }
}