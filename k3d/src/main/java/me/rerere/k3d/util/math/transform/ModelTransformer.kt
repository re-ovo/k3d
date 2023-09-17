package me.rerere.k3d.util.math.transform

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

internal fun Actor.setModelMatrix(matrix4: Matrix4) {
    val copy = matrix4.copy()

    val translation = parseTranslationMatrix(copy)
    val scale = parseScaleMatrix(copy)
    this.position.set(translation)
    this.scale.set(scale)

    removeScale(copy, scale)

    this.rotation.set(Quaternion.fromMatrix(copy))
}

internal fun translationMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        1f, 0f, 0f, x,
        0f, 1f, 0f, y,
        0f, 0f, 1f, z,
        0f, 0f, 0f, 1f
    )
}

internal fun translationMatrix(vec3: Vec3): Matrix4 {
    return translationMatrix(vec3.x, vec3.y, vec3.z)
}

internal fun parseTranslationMatrix(matrix4: Matrix4): Vec3 {
    return Vec3(matrix4[0, 3], matrix4[1, 3], matrix4[2, 3])
}

internal fun scaleMatrix(x: Float, y: Float, z: Float): Matrix4 {
    return Matrix4(
        x, 0f, 0f, 0f,
        0f, y, 0f, 0f,
        0f, 0f, z, 0f,
        0f, 0f, 0f, 1f
    )
}

internal fun scaleMatrix(vec3: Vec3): Matrix4 {
    return scaleMatrix(vec3.x, vec3.y, vec3.z)
}

internal fun parseScaleMatrix(matrix4: Matrix4): Vec3 {
    val x = sqrt(
        matrix4[0, 0] * matrix4[0, 0] +
                matrix4[0, 1] * matrix4[0, 1] +
                matrix4[0, 2] * matrix4[0, 2]
    )
    val y = sqrt(
        matrix4[1, 0] * matrix4[1, 0] +
                matrix4[1, 1] * matrix4[1, 1] +
                matrix4[1, 2] * matrix4[1, 2]
    )
    val z = sqrt(
        matrix4[2, 0] * matrix4[2, 0] +
                matrix4[2, 1] * matrix4[2, 1] +
                matrix4[2, 2] * matrix4[2, 2]
    )
    return Vec3(x, y, z)
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
        cosY * cosZ, sinX * sinY * cosZ - cosX * sinZ, cosX * sinY * cosZ + sinX * sinZ, 0f,
        cosY * sinZ, sinX * sinY * sinZ + cosX * cosZ, cosX * sinY * sinZ - sinX * cosZ, 0f,
        -sinY, sinX * cosY, cosX * cosY, 0f,
        0f, 0f, 0f, 1f
    )
}

internal fun rotationMatrix(vec3: Vec3): Matrix4 {
    return rotationMatrix(vec3.x, vec3.y, vec3.z)
}

//internal fun parseRotationMatrix(matrix4: Matrix4): Quaternion {
//    val x = matrix4[1, 2]
//    val y = matrix4[2, 0]
//    val z = matrix4[0, 1]
//    return Vec3(x, y, z)
//}