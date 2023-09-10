package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.Dirty
import kotlin.math.sqrt

class Quaternion(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var w: Float = 1f
): Dirty {
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

    override var dirty: Boolean = false

    companion object {
        fun identity(): Quaternion {
            return Quaternion()
        }
    }

    fun copy(): Quaternion {
        return Quaternion(x, y, z, w)
    }

    fun conjugate(): Quaternion {
        return Quaternion(-x, -y, -z, w)
    }

    fun selfConjugate() {
        x = -x
        y = -y
        z = -z
    }

    fun length(): Float {
        return sqrt(x * x + y * y + z * z + w * w)
    }

    fun normalize(): Quaternion {
        val length = length()
        return Quaternion(x / length, y / length, z / length, w / length)
    }

    fun selfNormalize() {
        val length = length()
        x /= length
        y /= length
        z /= length
        w /= length
    }
}