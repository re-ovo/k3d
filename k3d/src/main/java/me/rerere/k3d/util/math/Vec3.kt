package me.rerere.k3d.util.math

import me.rerere.k3d.util.Dirty
import kotlin.math.sqrt

class Vec3(
    x: Float = 0.0f,
    y: Float = 0.0f,
    z: Float = 0.0f
) : Dirty {
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

    override var dirty: Boolean = true

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }

    fun selfPlus(other: Vec3): Vec3 {
        x += other.x
        y += other.y
        z += other.z
        return this
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }

    fun selfMinus(other: Vec3): Vec3 {
        x -= other.x
        y -= other.y
        z -= other.z
        return this
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(x * other.x, y * other.y, z * other.z)
    }

    fun selfTimes(other: Vec3): Vec3 {
        x *= other.x
        y *= other.y
        z *= other.z
        return this
    }

    operator fun div(other: Vec3): Vec3 {
        return Vec3(x / other.x, y / other.y, z / other.z)
    }

    fun selfDiv(other: Vec3): Vec3 {
        x /= other.x
        y /= other.y
        z /= other.z
        return this
    }

    operator fun times(other: Float): Vec3 {
        return Vec3(x * other, y * other, z * other)
    }

    fun selfTimes(other: Float): Vec3 {
        x *= other
        y *= other
        z *= other
        return this
    }

    operator fun div(other: Float): Vec3 {
        return Vec3(x / other, y / other, z / other)
    }

    fun selfDiv(other: Float): Vec3 {
        x /= other
        y /= other
        z /= other
        return this
    }

    fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vec3): Vec3 {
        return Vec3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    fun distance(other: Vec3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vec3 {
        val length = length()
        return Vec3(x / length, y / length, z / length)
    }

    fun selfNormalize(): Vec3 {
        val length = length()
        x /= length
        y /= length
        z /= length
        return this
    }

    fun copy(): Vec3 {
        return Vec3(x, y, z)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Vec3) return false
        return x == other.x && y == other.y && z == other.z
    }

    override fun toString(): String {
        return "Vec3($x, $y, $z)"
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}