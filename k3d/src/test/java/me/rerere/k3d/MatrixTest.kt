package me.rerere.k3d

import me.rerere.k3d.util.math.Matrix4

import org.junit.Assert.*
import org.junit.Test

class MatrixTest {
    @Test
    fun testMatrixTimes() {
        val a = Matrix4(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )

        val b = Matrix4(
            1f, 2f, 3f, 4f,
            5f, 6f, 7f, 8f,
            9f, 10f, 11f, 12f,
            13f, 14f, 15f, 16f
        )

        assertEquals(a * b, Matrix4(
            90f, 100f, 110f, 120f,
            202f, 228f, 254f, 280f,
            314f, 356f, 398f, 440f,
            426f, 484f, 542f, 600f
        )
        )
    }

    @Test
    fun testInverse() {
        val matrix = Matrix4(
            1f, 0f, 0f, 2f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        assertEquals(matrix.inverse(), Matrix4(
            1f, 0f, 0f, -2f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
        )
        assertEquals(matrix * matrix.inverse(), Matrix4.identity())
        assertEquals(matrix.inverse().inverse(), matrix)
    }
}