@file:Suppress("MemberVisibilityCanBePrivate")

package me.rerere.k3d.scene

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.transform.rotationMatrix
import me.rerere.k3d.util.math.transform.scaleMatrix
import me.rerere.k3d.util.math.transform.translationMatrix

// 所有的场景元素都继承自Actor
open class Actor {
    private val position = Vec3(0f, 0f, 0f)
    private val translation = Vec3(0f, 0f, 0f)
    private val rotation = Vec3(0f, 0f, 0f)
    private val scale = Vec3(1f, 1f, 1f)

    private var _worldMatrix = Matrix4.identity()
    val worldMatrix: Matrix4
        get() = _worldMatrix

    /**
     * Update the world matrix
     *
     * You usually don't need to call this method manually
     */
    fun updateWorldMatrix() {
        this._worldMatrix = translationMatrix(position.x, position.y, position.z) *
                rotationMatrix(rotation.x, rotation.y, rotation.z) *
                scaleMatrix(scale.x, scale.y, scale.z)
    }

    /**
     * Get the position of this actor
     *
     * @return the position of this actor
     */
    fun getPosition(): Vec3 {
        return position
    }

    /**
     * Set the position of this actor
     *
     * @param x the x coordinate of the position
     * @param y the y coordinate of the position
     * @param z the z coordinate of the position
     */
    fun setPosition(x: Float, y: Float, z: Float) {
        position.x = x
        position.y = y
        position.z = z

        updateWorldMatrix()
    }

    /**
     * Get the rotation of this actor
     *
     * Note: the rotation is in radians
     *
     * @return the rotation of this actor
     */
    fun getRotation(): Vec3 {
        return rotation
    }

    /**
     * Set the rotation of this actor
     *
     * Note: the rotation is in radians
     *
     * @param x the rotation around x axis
     * @param y the rotation around y axis
     * @param z the rotation around z axis
     */
    fun setRotation(x: Float, y: Float, z: Float) {
        rotation.x = x
        rotation.y = y
        rotation.z = z

        updateWorldMatrix()
    }

    /**
     * Get the scale of this actor
     *
     * @return the scale of this actor
     */
    fun getScale(): Vec3 {
        return scale
    }

    /**
     * Set the scale of this actor
     *
     * @param x the scale on x axis
     */
    fun setScale(x: Float, y: Float, z: Float) {
        scale.x = x
        scale.y = y
        scale.z = z

        updateWorldMatrix()
    }
}