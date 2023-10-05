package me.rerere.k3d.util.math.transform

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import kotlin.math.cos
import kotlin.math.sin

internal fun Matrix4.applyTranslation(x: Float, y: Float, z: Float): Matrix4 {
    this[0, 3] += x
    this[1, 3] += y
    this[2, 3] += z
    return this
}

internal fun Matrix4.applyScale(x: Float, y: Float, z: Float): Matrix4 {
    this[0, 0] *= x
    this[0, 1] *= x
    this[0, 2] *= x
    this[1, 0] *= y
    this[1, 1] *= y
    this[1, 2] *= y
    this[2, 0] *= z
    this[2, 1] *= z
    this[2, 2] *= z
    return this
}

internal fun Matrix4.applyRotation(quaternion: Quaternion) = this.multiply(quaternion.toMatrix4())

internal fun translationMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        floatArrayOf(
            1f, 0f, 0f, x,
            0f, 1f, 0f, y,
            0f, 0f, 1f, z,
            0f, 0f, 0f, 1f
        )
    )
}

internal fun translationMatrix(vec3: Vec3): Matrix4 {
    return translationMatrix(vec3.x, vec3.y, vec3.z)
}

internal fun scaleMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        floatArrayOf(
            x, 0f, 0f, 0f,
            0f, y, 0f, 0f,
            0f, 0f, z, 0f,
            0f, 0f, 0f, 1f
        )
    )
}

internal fun scaleMatrix(vec3: Vec3): Matrix4 {
    return scaleMatrix(vec3.x, vec3.y, vec3.z)
}


internal fun removeScale(matrix: Matrix4, scale: Vec3) {
    matrix[0, 0] /= scale.x
    matrix[0, 1] /= scale.x
    matrix[0, 2] /= scale.x
    matrix[1, 0] /= scale.y
    matrix[1, 1] /= scale.y
    matrix[1, 2] /= scale.y
    matrix[2, 0] /= scale.z
    matrix[2, 1] /= scale.z
    matrix[2, 2] /= scale.z
}

internal fun rotationMatrix(x: Float, y: Float, z: Float): Matrix4 {
    val cosX = cos(x)
    val sinX = sin(x)
    val cosY = cos(y)
    val sinY = sin(y)
    val cosZ = cos(z)
    val sinZ = sin(z)
    return Matrix4(
        floatArrayOf(
            cosY * cosZ, sinX * sinY * cosZ - cosX * sinZ, cosX * sinY * cosZ + sinX * sinZ, 0f,
            cosY * sinZ, sinX * sinY * sinZ + cosX * cosZ, cosX * sinY * sinZ - sinX * cosZ, 0f,
            -sinY, sinX * cosY, cosX * cosY, 0f,
            0f, 0f, 0f, 1f
        )
    )
}

internal fun rotationMatrix(vec3: Vec3): Matrix4 {
    return rotationMatrix(vec3.x, vec3.y, vec3.z)
}