package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.system.Dirty
import kotlin.math.cos
import kotlin.math.sin

/**
 * [Euler angle](https://en.wikipedia.org/wiki/Euler_angles)
 *
 * This class represents a euler angle, which is a rotation in 3D space.
 * The rotation is represented by 3 angles, which are the rotation around
 * x, y and z axis. The rotation order is x -> y -> z.
 *
 * Note that the euler angle may cause [gimbal lock](https://en.wikipedia.org/wiki/Gimbal_lock).
 * If you want to avoid gimbal lock, use [Quaternion] instead.
 *
 * @property x x angle in radians
 * @property y y angle in radians
 * @property z z angle in radians
 * @property order rotation order
 */
class Euler(
    x: Float = 0f,
    y: Float = 0f,
    z: Float = 0f,
): Dirty {
    var x: Float = x
        set(value) {
            field = value
            this.markDirty()
        }
    var y: Float = y
        set(value) {
            field = value
            this.markDirty()
        }
    var z: Float = z
        set(value) {
            field = value
            this.markDirty()
        }

    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    override var dirty: Boolean = false

    fun toQuaternion(): Quaternion {
        val c1 = cos(x / 2)
        val c2 = cos(y / 2)
        val c3 = cos(z / 2)
        val s1 = sin(x / 2)
        val s2 = sin(y / 2)
        val s3 = sin(z / 2)

        return Quaternion(
            s1 * c2 * c3 + c1 * s2 * s3,
            c1 * s2 * c3 - s1 * c2 * s3,
            c1 * c2 * s3 + s1 * s2 * c3,
            c1 * c2 * c3 - s1 * s2 * s3
        )
    }

    override fun toString(): String {
        return "Euler(x=$x, y=$y, z=$z)"
    }

    override fun equals(other: Any?): Boolean {
        if(other !is Euler) return false
        return x == other.x && y == other.y && z == other.z
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }
}