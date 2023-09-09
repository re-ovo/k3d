package me.rerere.k3d.util.math.rotation

import me.rerere.k3d.util.math.Matrix4
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
data class Euler(
    var x: Float,
    var y: Float,
    var z: Float,
    val order: RotationOrder = RotationOrder.XYZ
) {
    fun set(x: Float, y: Float, z: Float) {
        this.x = x
        this.y = y
        this.z = z
    }

    fun toRotationMatrix(): Matrix4 {
        val cx = cos(x)
        val cy = cos(y)
        val cz = cos(z)
        val sx = sin(x)
        val sy = sin(y)
        val sz = sin(z)
        return when (order) {
            RotationOrder.XYZ -> Matrix4(
                cy * cz, -cy * sz, sy, 0f,
                sx * sy * cz + cx * sz, -sx * sy * sz + cx * cz, -sx * cy, 0f,
                -cx * sy * cz + sx * sz, cx * sy * sz + sx * cz, cx * cy, 0f,
                0f, 0f, 0f, 1f
            )

            RotationOrder.XZY -> Matrix4(
                cy * cz, -sz, sy * cz, 0f,
                sx * sy * cz + cx * sz, cx * cz, -sx * sy * sz + cx * cz, 0f,
                -cx * sy * cz + sx * sz, sx * cz, cx * cy, 0f,
                0f, 0f, 0f, 1f
            )

            RotationOrder.YXZ -> Matrix4(
                cy * cz + sy * sx * sz, -cy * sz + sy * sx * cz, sy * cx, 0f,
                cx * sz, cx * cz, -sx, 0f,
                -sy * cz + cy * sx * sz, sy * sz + cy * sx * cz, cy * cx, 0f,
                0f, 0f, 0f, 1f
            )

            RotationOrder.YZX -> Matrix4(
                cy * cz, -sz, sy * cz, 0f,
                sx * sy * cz + cx * sz, cx * cz, -sx * sy * sz + cx * cz, 0f,
                -cx * sy * cz + sx * sz, sx * cz, cx * cy, 0f,
                0f, 0f, 0f, 1f
            )

            RotationOrder.ZXY -> Matrix4(
                cy * cz - sy * sx * sz, -cx * sz, sy * cz + cy * sx * sz, 0f,
                cy * sz + sy * sx * cz, cx * cz, sy * sz - cy * sx * cz, 0f,
                -cx * sy, sx, cx * cy, 0f,
                0f, 0f, 0f, 1f
            )

            RotationOrder.ZYX -> Matrix4(
                cy * cz, -cy * sz, sy, 0f,
                sx * sy * cz + cx * sz, -sx * sy * sz + cx * cz, -sx * cy, 0f,
                -cx * sy * cz + sx * sz, cx * sy * sz + sx * cz, cx * cy, 0f,
                0f, 0f, 0f, 1f
            )
        }
    }

    fun clone() = Euler(x, y, z, order)
}

/**
 * Rotation order
 *
 * Different rotation order will cause different rotation result.
 * But I recommend you to use [RotationOrder.XYZ] as it's the most
 *
 * @see Euler
 */
enum class RotationOrder {
    XYZ, XZY, YXZ, YZX, ZXY, ZYX
}