package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.Dirty

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
        val c1 = kotlin.math.cos(x / 2)
        val c2 = kotlin.math.cos(y / 2)
        val c3 = kotlin.math.cos(z / 2)
        val s1 = kotlin.math.sin(x / 2)
        val s2 = kotlin.math.sin(y / 2)
        val s3 = kotlin.math.sin(z / 2)

        return Quaternion(
            s1 * c2 * c3 + c1 * s2 * s3,
            c1 * s2 * c3 - s1 * c2 * s3,
            c1 * c2 * s3 + s1 * s2 * c3,
            c1 * c2 * c3 - s1 * s2 * s3
        )
    }
}