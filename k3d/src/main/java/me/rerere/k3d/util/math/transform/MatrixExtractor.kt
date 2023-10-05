package me.rerere.k3d.util.math.transform

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.Quaternion
import kotlin.math.sqrt

internal fun Actor.setModelMatrix(matrix4: Matrix4) {
    val copy = matrix4.copy()

    val translation = extractTranslationMatrix(copy)
    val scale = extractScaleMatrix(copy)
    this.position.set(translation)
    this.scale.set(scale)

    removeScale(copy, scale)

    this.rotation.set(Quaternion.fromMatrix(copy))
}

internal fun extractTranslationMatrix(matrix4: Matrix4): Vec3 {
    return Vec3(matrix4[0, 3], matrix4[1, 3], matrix4[2, 3])
}

internal fun extractScaleMatrix(matrix4: Matrix4): Vec3 {
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