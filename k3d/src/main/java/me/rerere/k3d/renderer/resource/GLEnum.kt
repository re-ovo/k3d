package me.rerere.k3d.renderer.resource

import android.opengl.GLES20

/**
 * OpenGL ES Data Type
 *
 * @property value OpenGL ES Data Type
 * @property size Size of one item in bytes
 */
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

enum class DrawMode(val value: Int) {
    POINTS(GLES20.GL_POINTS),
    LINES(GLES20.GL_LINES),
    LINE_LOOP(GLES20.GL_LINE_LOOP),
    LINE_STRIP(GLES20.GL_LINE_STRIP),
    TRIANGLES(GLES20.GL_TRIANGLES),
    TRIANGLE_STRIP(GLES20.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLES20.GL_TRIANGLE_FAN);
}