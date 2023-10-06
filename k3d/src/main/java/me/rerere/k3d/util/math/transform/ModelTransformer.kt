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

internal fun Matrix4.applyEulerRotation(x: Float, y: Float, z: Float) {
    val cosX = cos(x)
    val sinX = sin(x)
    val cosY = cos(y)
    val sinY = sin(y)
    val cosZ = cos(z)
    val sinZ = sin(z)

    val m00 = cosY * cosZ
    val m01 = sinX * sinY * cosZ - cosX * sinZ
    val m02 = cosX * sinY * cosZ + sinX * sinZ
    val m10 = cosY * sinZ
    val m11 = sinX * sinY * sinZ + cosX * cosZ
    val m12 = cosX * sinY * sinZ - sinX * cosZ
    val m20 = -sinY
    val m21 = sinX * cosY
    val m22 = cosX * cosY

    val nm00 = this[0, 0] * m00 + this[0, 1] * m10 + this[0, 2] * m20
    val nm01 = this[0, 0] * m01 + this[0, 1] * m11 + this[0, 2] * m21
    val nm02 = this[0, 0] * m02 + this[0, 1] * m12 + this[0, 2] * m22
    val nm03 = this[0, 3]

    val nm10 = this[1, 0] * m00 + this[1, 1] * m10 + this[1, 2] * m20
    val nm11 = this[1, 0] * m01 + this[1, 1] * m11 + this[1, 2] * m21
    val nm12 = this[1, 0] * m02 + this[1, 1] * m12 + this[1, 2] * m22
    val nm13 = this[1, 3]

    val nm20 = this[2, 0] * m00 + this[2, 1] * m10 + this[2, 2] * m20
    val nm21 = this[2, 0] * m01 + this[2, 1] * m11 + this[2, 2] * m21
    val nm22 = this[2, 0] * m02 + this[2, 1] * m12 + this[2, 2] * m22
    val nm23 = this[2, 3]

    val nm30 = this[3, 0] * m00 + this[3, 1] * m10 + this[3, 2] * m20
    val nm31 = this[3, 0] * m01 + this[3, 1] * m11 + this[3, 2] * m21
    val nm32 = this[3, 0] * m02 + this[3, 1] * m12 + this[3, 2] * m22
    val nm33 = this[3, 3]

    this[0, 0] = nm00
    this[0, 1] = nm01
    this[0, 2] = nm02
    this[0, 3] = nm03

    this[1, 0] = nm10
    this[1, 1] = nm11
    this[1, 2] = nm12
    this[1, 3] = nm13

    this[2, 0] = nm20
    this[2, 1] = nm21
    this[2, 2] = nm22
    this[2, 3] = nm23

    this[3, 0] = nm30
    this[3, 1] = nm31
    this[3, 2] = nm32
    this[3, 3] = nm33
}

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