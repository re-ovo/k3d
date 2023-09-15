package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.Dirty
import me.rerere.k3d.util.math.Matrix4
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