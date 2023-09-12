package me.rerere.k3d.renderer.resource

import android.opengl.GLES20

enum class DataType(
    val value: Int,
    val size: Int
) {
    FLOAT(GLES20.GL_FLOAT, 4),
    INT(GLES20.GL_INT, 4),
    UNSIGNED_INT(GLES20.GL_UNSIGNED_INT, 4),
    SHORT(GLES20.GL_SHORT, 2),
    UNSIGNED_SHORT(GLES20.GL_UNSIGNED_SHORT, 2),
    BYTE(GLES20.GL_BYTE, 1),
    UNSIGNED_BYTE(GLES20.GL_UNSIGNED_BYTE, 1)
}