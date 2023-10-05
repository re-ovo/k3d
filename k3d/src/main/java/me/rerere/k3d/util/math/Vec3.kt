package me.rerere.k3d.util.math

import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.dirtyFloatValue
import me.rerere.k3d.util.system.dirtyValue
import kotlin.math.sqrt

class Vec3(
    x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f
) : Dirty {
    private var _dirty = false

    var x by dirtyFloatValue(x)
    var y by dirtyFloatValue(y)
    var z by dirtyFloatValue(z)

    override fun isDirty(): Boolean = _dirty

    override fun updateDirty() {}

    override fun markDirtyNew() {
        _dirty = true
    }

    override fun clearDirty() {
        _dirty = false
    }

    fun set(x: Float, y: Float, z: Float): Vec3 {
        this.x = x
        this.y = y
        this.z = z
        return this
    }

    fun set(other: Vec3): Vec3 {
        this.x = other.x
        this.y = other.y
        this.z = other.z
        return this
    }

    operator fun plus(other: Vec3): Vec3 {
        return Vec3(x + other.x, y + other.y, z + other.z)
    }

    operator fun plusAssign(other: Vec3) {
        x += other.x
        y += other.y
        z += other.z
    }

    operator fun minus(other: Vec3): Vec3 {
        return Vec3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Vec3): Vec3 {
        return Vec3(x * other.x, y * other.y, z * other.z)
    }

    operator fun div(other: Vec3): Vec3 {
        return Vec3(x / other.x, y / other.y, z / other.z)
    }

    operator fun times(other: Float): Vec3 {
        return Vec3(x * other, y * other, z * other)
    }

    operator fun timesAssign(other: Float) {
        x *= other
        y *= other
        z *= other
    }

    operator fun div(other: Float): Vec3 {
        return Vec3(x / other, y / other, z / other)
    }

    fun dot(other: Vec3): Float {
        return x * other.x + y * other.y + z * other.z
    }

    fun cross(other: Vec3): Vec3 {
        return Vec3(
            y * other.z - z * other.y, z * other.x - x * other.z, x * other.y - y * other.x
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

    fun normalizeSelf(): Vec3 {
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