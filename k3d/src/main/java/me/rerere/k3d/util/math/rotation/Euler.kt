package me.rerere.k3d.util.math.rotation

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
 */
class Euler(
    var x: Float,
    var y: Float,
    var z: Float,
    val order: RotationOrder = RotationOrder.XYZ
)

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

fun main() {
    val euler = Euler(0f, 0f, 0f.toRadian())
    println(euler)
}