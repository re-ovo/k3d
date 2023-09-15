package me.rerere.k3d.util.math.transform

import me.rerere.k3d.util.math.Matrix4

internal fun perspectiveMatrix(
    left: Float,
    right: Float,
    bottom: Float,
    top: Float,
    near: Float,
    far: Float
): Matrix4 {
    val x = 2 * near / (right - left)
    val y = 2 * near / (top - bottom)
    val a = (right + left) / (right - left)
    val b = (top + bottom) / (top - bottom)
    val c = -(far + near) / (far - near)
    val d = -2 * far * near / (far - near)
    return Matrix4(
        x, 0f, a, 0f,
        0f, y, b, 0f,
        0f, 0f, c, d,
        0f, 0f, -1f, 0f
    )
}