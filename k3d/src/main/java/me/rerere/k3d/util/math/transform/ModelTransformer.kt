package me.rerere.k3d.util.math.transform

import me.rerere.k3d.util.math.Matrix4
import kotlin.math.cos
import kotlin.math.sin

fun translationMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        1f, 0f, 0f, x,
        0f, 1f, 0f, y,
        0f, 0f, 1f, z,
        0f, 0f, 0f, 1f
    )
}

fun scaleMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        x, 0f, 0f, 0f,
        0f, y, 0f, 0f,
        0f, 0f, z, 0f,
        0f, 0f, 0f, 1f
    )
}

fun rotationMatrix(x: Float, y: Float, z: Float): Matrix4 {
    val cosX = cos(x)
    val sinX = sin(x)
    val cosY = cos(y)
    val sinY = sin(y)
    val cosZ = cos(z)
    val sinZ = sin(z)
    return Matrix4(
        cosY * cosZ, cosY * sinZ, -sinY, 0f,
        sinX * sinY * cosZ - cosX * sinZ, sinX * sinY * sinZ + cosX * cosZ, sinX * cosY, 0f,
        cosX * sinY * cosZ + sinX * sinZ, cosX * sinY * sinZ - sinX * cosZ, cosX * cosY, 0f,
        0f, 0f, 0f, 1f
    )
}