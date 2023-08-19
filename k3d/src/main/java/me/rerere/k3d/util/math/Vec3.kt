package me.rerere.k3d.util.math

import kotlin.math.sqrt

class Vec3(
    var x: Float = 0.0f,
    var y: Float = 0.0f,
    var z: Float = 0.0f
) {
    operator fun plus(other: Vec3): Vec3 {
        return Vec3(
            x + other.x,
            y + other.y,
            z + other.z
        )
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(
            x - other.x,
            y - other.y,
            z - other.z
        )
    }

    operator fun unaryMinus(): Vec3 {
        return Vec3(
            -x,
            -y,
            -z
        )
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        )
    }

    operator fun times(other: Float): Vec3 {
        return Vec3(
            x * other,
            y * other,
            z * other
        )
    }

    operator fun div(other: Float): Vec3 {
        return Vec3(
            x / other,
            y / other,
            z / other
        )
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z)
    }

    fun normalize(): Vec3 {
        return this / length()
    }

    fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vec3): Vec3 {
        return this * other
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Vec3) return false
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