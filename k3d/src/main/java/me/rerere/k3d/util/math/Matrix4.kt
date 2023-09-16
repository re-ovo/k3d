package me.rerere.k3d.util.math

/**
 * Represents a 4x4 matrix in 3D space.
 */
class Matrix4(vararg val data: Float) {
    init {
        if (data.size != 16) {
            throw IllegalArgumentException("Matrix4 must have 16 elements")
        }
    }

    companion object {
        fun identity() = Matrix4(
            1f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f,
            0f, 0f, 1f, 0f,
            0f, 0f, 0f, 1f
        )
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
        return Matrix4(*result)
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
        for (i in 0..3) {
            for (j in 0..3) {
                result[i * 4 + j] = this.data[j * 4 + i]
            }
        }
        return Matrix4(*result)
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

        return Matrix4(*inv)
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

