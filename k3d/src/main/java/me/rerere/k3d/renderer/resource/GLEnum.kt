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
    UNSIGNED_BYTE(GLES20.GL_UNSIGNED_BYTE, 1);

    companion object {
        fun fromValue(value: Int): DataType {
            return when (value) {
                GLES20.GL_FLOAT -> FLOAT
                GLES20.GL_INT -> INT
                GLES20.GL_UNSIGNED_INT -> UNSIGNED_INT
                GLES20.GL_SHORT -> SHORT
                GLES20.GL_UNSIGNED_SHORT -> UNSIGNED_SHORT
                GLES20.GL_BYTE -> BYTE
                GLES20.GL_UNSIGNED_BYTE -> UNSIGNED_BYTE
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }
}

/**
 * OpenGL Draw Mode
 *
 * It is used to specify how the vertices are connected, and how the primitives are rendered.
 * The most common ones are [TRIANGLES], [LINES], [POINTS].
 *
 * @property value OpenGL Draw Mode
 */
enum class DrawMode(val value: Int) {
    POINTS(GLES20.GL_POINTS),
    LINES(GLES20.GL_LINES),
    LINE_LOOP(GLES20.GL_LINE_LOOP),
    LINE_STRIP(GLES20.GL_LINE_STRIP),
    TRIANGLES(GLES20.GL_TRIANGLES),
    TRIANGLE_STRIP(GLES20.GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GLES20.GL_TRIANGLE_FAN);

    companion object {
        fun fromValue(value: Int): DrawMode {
            return when (value) {
                GLES20.GL_POINTS -> POINTS
                GLES20.GL_LINES -> LINES
                GLES20.GL_LINE_LOOP -> LINE_LOOP
                GLES20.GL_LINE_STRIP -> LINE_STRIP
                GLES20.GL_TRIANGLES -> TRIANGLES
                GLES20.GL_TRIANGLE_STRIP -> TRIANGLE_STRIP
                GLES20.GL_TRIANGLE_FAN -> TRIANGLE_FAN
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }
}

enum class TextureFilter(val value: Int) {
    NEAREST(GLES20.GL_NEAREST),
    LINEAR(GLES20.GL_LINEAR),
    NEAREST_MIPMAP_NEAREST(GLES20.GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GLES20.GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GLES20.GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GLES20.GL_LINEAR_MIPMAP_LINEAR);

    companion object {
        fun fromValue(value: Int): TextureFilter {
            return when (value) {
                GLES20.GL_NEAREST -> NEAREST
                GLES20.GL_LINEAR -> LINEAR
                GLES20.GL_NEAREST_MIPMAP_NEAREST -> NEAREST_MIPMAP_NEAREST
                GLES20.GL_LINEAR_MIPMAP_NEAREST -> LINEAR_MIPMAP_NEAREST
                GLES20.GL_NEAREST_MIPMAP_LINEAR -> NEAREST_MIPMAP_LINEAR
                GLES20.GL_LINEAR_MIPMAP_LINEAR -> LINEAR_MIPMAP_LINEAR
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }
}

enum class TextureWrap(val value: Int) {
    REPEAT(GLES20.GL_REPEAT),
    CLAMP_TO_EDGE(GLES20.GL_CLAMP_TO_EDGE),
    MIRRORED_REPEAT(GLES20.GL_MIRRORED_REPEAT);

    companion object {
        fun fromValue(value: Int): TextureWrap {
            return when (value) {
                GLES20.GL_REPEAT -> REPEAT
                GLES20.GL_CLAMP_TO_EDGE -> CLAMP_TO_EDGE
                GLES20.GL_MIRRORED_REPEAT -> MIRRORED_REPEAT
                else -> throw IllegalArgumentException("Unknown value: $value")
            }
        }
    }
}