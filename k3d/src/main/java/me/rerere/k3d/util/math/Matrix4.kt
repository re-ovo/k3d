package me.rerere.k3d.util.math

import java.util.Arrays

/**
 * Represents a 4x4 matrix in 3D space.
 */
class Matrix4(val data: FloatArray) {
    init {
        if (data.size != 16) {
            throw IllegalArgumentException("Matrix4 must have 16 elements")
        }
    }

    companion object {
        fun identity() = Matrix4(
            floatArrayOf(
                1f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f,
                0f, 0f, 1f, 0f,
                0f, 0f, 0f, 1f
            )
        )

        fun zero() = Matrix4(
            floatArrayOf(
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f,
                0f, 0f, 0f, 0f
            )
        )

        fun fromColumnMajor(data: FloatArray): Matrix4 {
            if (data.size != 16) {
                throw IllegalArgumentException("Matrix4 must have 16 elements")
            }
            return Matrix4(
                floatArrayOf(
                    data[0], data[4], data[8], data[12],
                    data[1], data[5], data[9], data[13],
                    data[2], data[6], data[10], data[14],
                    data[3], data[7], data[11], data[15]
                )
            )
        }

        fun fromColumnMajor(data: List<Float>): Matrix4 {
            if (data.size != 16) {
                throw IllegalArgumentException("Matrix4 must have 16 elements")
            }
            return Matrix4(
                floatArrayOf(
                    data[0], data[4], data[8], data[12],
                    data[1], data[5], data[9], data[13],
                    data[2], data[6], data[10], data[14],
                    data[3], data[7], data[11], data[15]
                )
            )
        }
    }

    fun set(data: FloatArray) {
        if (data.size != 16) {
            throw IllegalArgumentException("Matrix4 must have 16 elements")
        }
        System.arraycopy(data, 0, this.data, 0, 16)
    }

    fun set(other: Matrix4) {
        System.arraycopy(other.data, 0, this.data, 0, 16)
    }

    operator fun get(row: Int, col: Int): Float {
        return this.data[row * 4 + col]
    }

    operator fun set(row: Int, col: Int, value: Float) {
        this.data[row * 4 + col] = value
    }

    operator fun times(other: Matrix4): Matrix4 {
        val result = FloatArray(16)
        for (i in 0..3) {
            for (j in 0..3) {
                result[i * 4 + j] = 0f
                for (k in 0..3) {
                    result[i * 4 + j] += this.data[i * 4 + k] * other.data[k * 4 + j]
                }
            }
        }

        result[0] = this.data[0] * other.data[0] + this.data[1] * other.data[4] + this.data[2] * other.data[8] + this.data[3] * other.data[12]
        result[1] = this.data[0] * other.data[1] + this.data[1] * other.data[5] + this.data[2] * other.data[9] + this.data[3] * other.data[13]
        result[2] = this.data[0] * other.data[2] + this.data[1] * other.data[6] + this.data[2] * other.data[10] + this.data[3] * other.data[14]
        result[3] = this.data[0] * other.data[3] + this.data[1] * other.data[7] + this.data[2] * other.data[11] + this.data[3] * other.data[15]

        result[4] = this.data[4] * other.data[0] + this.data[5] * other.data[4] + this.data[6] * other.data[8] + this.data[7] * other.data[12]
        result[5] = this.data[4] * other.data[1] + this.data[5] * other.data[5] + this.data[6] * other.data[9] + this.data[7] * other.data[13]
        result[6] = this.data[4] * other.data[2] + this.data[5] * other.data[6] + this.data[6] * other.data[10] + this.data[7] * other.data[14]
        result[7] = this.data[4] * other.data[3] + this.data[5] * other.data[7] + this.data[6] * other.data[11] + this.data[7] * other.data[15]

        result[8] = this.data[8] * other.data[0] + this.data[9] * other.data[4] + this.data[10] * other.data[8] + this.data[11] * other.data[12]
        result[9] = this.data[8] * other.data[1] + this.data[9] * other.data[5] + this.data[10] * other.data[9] + this.data[11] * other.data[13]
        result[10] = this.data[8] * other.data[2] + this.data[9] * other.data[6] + this.data[10] * other.data[10] + this.data[11] * other.data[14]
        result[11] = this.data[8] * other.data[3] + this.data[9] * other.data[7] + this.data[10] * other.data[11] + this.data[11] * other.data[15]

        result[12] = this.data[12] * other.data[0] + this.data[13] * other.data[4] + this.data[14] * other.data[8] + this.data[15] * other.data[12]
        result[13] = this.data[12] * other.data[1] + this.data[13] * other.data[5] + this.data[14] * other.data[9] + this.data[15] * other.data[13]
        result[14] = this.data[12] * other.data[2] + this.data[13] * other.data[6] + this.data[14] * other.data[10] + this.data[15] * other.data[14]
        result[15] = this.data[12] * other.data[3] + this.data[13] * other.data[7] + this.data[14] * other.data[11] + this.data[15] * other.data[15]

        return Matrix4(result)
    }

    operator fun times(scalar: Float): Matrix4 {
        val result = FloatArray(16)
        for (i in 0..15) {
            result[i] = this.data[i] * scalar
        }
        return Matrix4(result)
    }

    operator fun times(vector: Vec4): Vec4 {
        val result = FloatArray(4)
        for (i in 0..3) {
            result[i] = 0f
            for (j in 0..3) {
                result[i] += this.data[i * 4 + j] * vector[j]
            }
        }
        return Vec4(result[0], result[1], result[2], result[3])
    }

    operator fun plus(other: Matrix4): Matrix4 {
        val result = FloatArray(16)
        for (i in 0..15) {
            result[i] = this.data[i] + other.data[i]
        }
        return Matrix4(result)
    }

    fun applyMatrix4(other: Matrix4): Matrix4 {
        val result = FloatArray(16)
        for (i in 0..3) {
            for (j in 0..3) {
                result[i * 4 + j] = 0f
                for (k in 0..3) {
                    result[i * 4 + j] += other.data[i * 4 + k] * this.data[k * 4 + j]
                }
            }
        }
        System.arraycopy(result, 0, this.data, 0, 16)
        return this
    }

    fun transpose(): Matrix4 {
        val result = FloatArray(16)

        result[0] = this.data[0]
        result[1] = this.data[4]
        result[2] = this.data[8]
        result[3] = this.data[12]

        result[4] = this.data[1]
        result[5] = this.data[5]
        result[6] = this.data[9]
        result[7] = this.data[13]

        result[8] = this.data[2]
        result[9] = this.data[6]
        result[10] = this.data[10]
        result[11] = this.data[14]

        result[12] = this.data[3]
        result[13] = this.data[7]
        result[14] = this.data[11]
        result[15] = this.data[15]

        return Matrix4(result)
    }

    fun inverse(): Matrix4 {
        val inv = FloatArray(16)

        inv[0] = data[5] * data[10] * data[15] -
                data[5] * data[11] * data[14] -
                data[9] * data[6] * data[15] +
                data[9] * data[7] * data[14] +
                data[13] * data[6] * data[11] -
                data[13] * data[7] * data[10]

        inv[4] = -data[4] * data[10] * data[15] +
                data[4] * data[11] * data[14] +
                data[8] * data[6] * data[15] -
                data[8] * data[7] * data[14] -
                data[12] * data[6] * data[11] +
                data[12] * data[7] * data[10]

        inv[8] = data[4] * data[9] * data[15] -
                data[4] * data[11] * data[13] -
                data[8] * data[5] * data[15] +
                data[8] * data[7] * data[13] +
                data[12] * data[5] * data[11] -
                data[12] * data[7] * data[9]

        inv[12] = -data[4] * data[9] * data[14] +
                data[4] * data[10] * data[13] +
                data[8] * data[5] * data[14] -
                data[8] * data[6] * data[13] -
                data[12] * data[5] * data[10] +
                data[12] * data[6] * data[9]

        inv[1] = -data[1] * data[10] * data[15] +
                data[1] * data[11] * data[14] +
                data[9] * data[2] * data[15] -
                data[9] * data[3] * data[14] -
                data[13] * data[2] * data[11] +
                data[13] * data[3] * data[10]

        inv[5] = data[0] * data[10] * data[15] -
                data[0] * data[11] * data[14] -
                data[8] * data[2] * data[15] +
                data[8] * data[3] * data[14] +
                data[12] * data[2] * data[11] -
                data[12] * data[3] * data[10]

        inv[9] = -data[0] * data[9] * data[15] +
                data[0] * data[11] * data[13] +
                data[8] * data[1] * data[15] -
                data[8] * data[3] * data[13] -
                data[12] * data[1] * data[11] +
                data[12] * data[3] * data[9]

        inv[13] = data[0] * data[9] * data[14] -
                data[0] * data[10] * data[13] -
                data[8] * data[1] * data[14] +
                data[8] * data[2] * data[13] +
                data[12] * data[1] * data[10] -
                data[12] * data[2] * data[9]

        inv[2] = data[1] * data[6] * data[15] -
                data[1] * data[7] * data[14] -
                data[5] * data[2] * data[15] +
                data[5] * data[3] * data[14] +
                data[13] * data[2] * data[7] -
                data[13] * data[3] * data[6]

        inv[6] = -data[0] * data[6] * data[15] +
                data[0] * data[7] * data[14] +
                data[4] * data[2] * data[15] -
                data[4] * data[3] * data[14] -
                data[12] * data[2] * data[7] +
                data[12] * data[3] * data[6]

        inv[10] = data[0] * data[5] * data[15] -
                data[0] * data[7] * data[13] -
                data[4] * data[1] * data[15] +
                data[4] * data[3] * data[13] +
                data[12] * data[1] * data[7] -
                data[12] * data[3] * data[5]

        inv[14] = -data[0] * data[5] * data[14] +
                data[0] * data[6] * data[13] +
                data[4] * data[1] * data[14] -
                data[4] * data[2] * data[13] -
                data[12] * data[1] * data[6] +
                data[12] * data[2] * data[5]

        inv[3] = -data[1] * data[6] * data[11] +
                data[1] * data[7] * data[10] +
                data[5] * data[2] * data[11] -
                data[5] * data[3] * data[10] -
                data[9] * data[2] * data[7] +
                data[9] * data[3] * data[6]

        inv[7] = data[0] * data[6] * data[11] -
                data[0] * data[7] * data[10] -
                data[4] * data[2] * data[11] +
                data[4] * data[3] * data[10] +
                data[8] * data[2] * data[7] -
                data[8] * data[3] * data[6]

        inv[11] = -data[0] * data[5] * data[11] +
                data[0] * data[7] * data[9] +
                data[4] * data[1] * data[11] -
                data[4] * data[3] * data[9] -
                data[8] * data[1] * data[7] +
                data[8] * data[3] * data[5]

        inv[15] = data[0] * data[5] * data[10] -
                data[0] * data[6] * data[9] -
                data[4] * data[1] * data[10] +
                data[4] * data[2] * data[9] +
                data[8] * data[1] * data[6] -
                data[8] * data[2] * data[5]

        val det = data[0] * inv[0] + data[1] * inv[4] + data[2] * inv[8] + data[3] * inv[12]

        if (det == 0f) {
            throw IllegalStateException("Matrix is singular and cannot be inverted")
        }

        val invDet = 1.0f / det
        for (i in 0..15) {
            inv[i] *= invDet
        }

        return Matrix4(inv)
    }

    fun copy(): Matrix4 {
        return Matrix4(this.data.copyOf(16))
    }

    override fun equals(other: Any?): Boolean {
        if (other !is Matrix4) {
            return false
        }
        return this.data.contentEquals(other.data)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        for (i in 0..3) {
            for (j in 0..3) {
                builder.append(this.data[i * 4 + j])
                builder.append(" ")
            }
            builder.append("\n")
        }
        return builder.toString()
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }
}

