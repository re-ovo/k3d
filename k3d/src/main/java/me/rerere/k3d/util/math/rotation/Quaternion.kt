package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.Dirty
import me.rerere.k3d.util.math.Matrix4
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Represents a quaternion in 3D space.
 *
 * A quaternion is a mathematical concept used to represent rotations in 3D space. It consists of four components: x, y, z, and w.
 * The x, y, and z components represent the imaginary part of the quaternion, while the w component represents the real part.
 *
 * Quaternions are commonly used in computer graphics and game development to represent rotations, as they provide a compact
 * and efficient way to interpolate between different orientations.
 *
 * The most important advantage of quaternions over other representations of rotations (such as Euler angles) is that they do not suffer from gimbal lock.
 *
 * @param x The x component of the quaternion (default value is 0).
 * @param y The y component of the quaternion (default value is 0).
 * @param z The z component of the quaternion (default value is 0).
 * @param w The w component of the quaternion (default value is 1).
 */
class Quaternion(
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
    w: Float = 1f
) : Dirty {
    override var dirty: Boolean = false
    var x = x
        set(value) {
            field = value
            markDirty()
        }
    var y = y
        set(value) {
            field = value
            markDirty()
        }
    var z = z
        set(value) {
            field = value
            markDirty()
        }
    var w = w
        set(value) {
            field = value
            markDirty()
        }

    fun set(x: Float, y: Float, z: Float, w: Float) {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
    }

    fun set(other: Quaternion) {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        this.w = other.w
    }

    fun copy(): Quaternion {
        return Quaternion(x, y, z, w)
    }

    fun conjugate(): Quaternion {
        return Quaternion(-x, -y, -z, w)
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z + w * w)
    }

    fun normalize(): Quaternion {
        val length = length()
        return Quaternion(x / length, y / length, z / length, w / length)
    }

    operator fun times(other: Quaternion): Quaternion {
        val x = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y
        val y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x
        val z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w
        val w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z
        return Quaternion(x, y, z, w)
    }

    operator fun timesAssign(other: Quaternion) {
        val x = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y
        val y = this.w * other.y - this.x * other.z + this.y * other.w + this.z * other.x
        val z = this.w * other.z + this.x * other.y - this.y * other.x + this.z * other.w
        val w = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z
        this.set(x, y, z, w)
    }

    fun applyRotation(other: Quaternion) {
        val newQuaternion = other * this
        this.set(newQuaternion)
    }

    fun toEuler() : Euler {
        val euler = Euler()
        val test = x * y + z * w
        if (test > 0.499) { // singularity at north pole
            euler.y = 2 * atan2(x, w)
            euler.x = (Math.PI / 2f).toFloat()
            euler.z = 0f
            return euler
        } else if (test < -0.499) { // singularity at south pole
            euler.y = -2 * atan2(x, w)
            euler.x = (-Math.PI / 2f).toFloat()
            euler.z = 0f
            return euler
        } else {
            val sqx = x * x
            val sqy = y * y
            val sqz = z * z
            euler.y = atan2(2 * y * w - 2 * x * z, 1 - 2 * sqy - 2 * sqz)
            euler.x = atan2(2 * x * w - 2 * y * z, 1 - 2 * sqx - 2 * sqz)
            euler.z = asin(2 * test)
            return euler
        }
    }

    fun toMatrix4(): Matrix4 {
        val xx = x * x
        val xy = x * y
        val xz = x * z
        val xw = x * w
        val yy = y * y
        val yz = y * z
        val yw = y * w
        val zz = z * z
        val zw = z * w
        return Matrix4(
            1f - 2f * (yy + zz), 2f * (xy - zw), 2f * (xz + yw), 0f,
            2f * (xy + zw), 1f - 2f * (xx + zz), 2f * (yz - xw), 0f,
            2f * (xz - yw), 2f * (yz + xw), 1f - 2f * (xx + yy), 0f,
            0f, 0f, 0f, 1f
        )
    }

    companion object {
        @JvmStatic
        fun identity(): Quaternion {
            return Quaternion()
        }
    }
}