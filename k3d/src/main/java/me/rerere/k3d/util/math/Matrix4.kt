package me.rerere.k3d.util.math

import java.nio.FloatBuffer

/**
 * Represents a 4x4 matrix in 3D space.
 *
 * This matrix is in row-major order to facilitate human understanding. However, when passing to
 * OpenGL, it should be converted to column-major order (OpenGL uses column-major order).
 *
 * This matrix implements operator overloading, you can use `+`, `*` and `[]` to access the matrix.
 * These overloaded operators are matrix-invariant by default, so it will create a new matrix as
 * the result. You should try to avoid using these operators in render loop or any other performance
 * critical code.
 *
 * To avoid memory allocation, you can use the following methods:
 *  - [multiplyMatrices]: multiply two matrices(a * b) and store the result in this matrix
 *  - [multiply]: multiply self with another matrix (self * other) and store the result in this matrix
 *  - [preMultiply]: multiply other with self (other * self) and store the result in this matrix
 *  - [multiplyToArray]: multiply self with another matrix and store the result in an array (already in column major).
 *  - [multiplyToFloatBuffer]: multiply self with another matrix and store the result in a float buffer (already in column major).
 *
 * The `*=` operator is also overloaded, which is equivalent to [multiply]
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

    fun setToIdentity(): Matrix4 {
        this.data[0] = 1f
        this.data[1] = 0f
        this.data[2] = 0f
        this.data[3] = 0f

        this.data[4] = 0f
        this.data[5] = 1f
        this.data[6] = 0f
        this.data[7] = 0f

        this.data[8] = 0f
        this.data[9] = 0f
        this.data[10] = 1f
        this.data[11] = 0f

        this.data[12] = 0f
        this.data[13] = 0f
        this.data[14] = 0f
        this.data[15] = 1f

        return this
    }

    operator fun get(row: Int, col: Int): Float {
        return this.data[row * 4 + col]
    }

    operator fun set(row: Int, col: Int, value: Float) {
        this.data[row * 4 + col] = value
    }

    operator fun get(index: Int): Float {
        return this.data[index]
    }

    operator fun set(index: Int, value: Float) {
        this.data[index] = value
    }

    operator fun times(other: Matrix4): Matrix4 {
        val result = FloatArray(16)

        result[0] =
            this.data[0] * other.data[0] + this.data[1] * other.data[4] + this.data[2] * other.data[8] + this.data[3] * other.data[12]
        result[1] =
            this.data[0] * other.data[1] + this.data[1] * other.data[5] + this.data[2] * other.data[9] + this.data[3] * other.data[13]
        result[2] =
            this.data[0] * other.data[2] + this.data[1] * other.data[6] + this.data[2] * other.data[10] + this.data[3] * other.data[14]
        result[3] =
            this.data[0] * other.data[3] + this.data[1] * other.data[7] + this.data[2] * other.data[11] + this.data[3] * other.data[15]

        result[4] =
            this.data[4] * other.data[0] + this.data[5] * other.data[4] + this.data[6] * other.data[8] + this.data[7] * other.data[12]
        result[5] =
            this.data[4] * other.data[1] + this.data[5] * other.data[5] + this.data[6] * other.data[9] + this.data[7] * other.data[13]
        result[6] =
            this.data[4] * other.data[2] + this.data[5] * other.data[6] + this.data[6] * other.data[10] + this.data[7] * other.data[14]
        result[7] =
            this.data[4] * other.data[3] + this.data[5] * other.data[7] + this.data[6] * other.data[11] + this.data[7] * other.data[15]

        result[8] =
            this.data[8] * other.data[0] + this.data[9] * other.data[4] + this.data[10] * other.data[8] + this.data[11] * other.data[12]
        result[9] =
            this.data[8] * other.data[1] + this.data[9] * other.data[5] + this.data[10] * other.data[9] + this.data[11] * other.data[13]
        result[10] =
            this.data[8] * other.data[2] + this.data[9] * other.data[6] + this.data[10] * other.data[10] + this.data[11] * other.data[14]
        result[11] =
            this.data[8] * other.data[3] + this.data[9] * other.data[7] + this.data[10] * other.data[11] + this.data[11] * other.data[15]

        result[12] =
            this.data[12] * other.data[0] + this.data[13] * other.data[4] + this.data[14] * other.data[8] + this.data[15] * other.data[12]
        result[13] =
            this.data[12] * other.data[1] + this.data[13] * other.data[5] + this.data[14] * other.data[9] + this.data[15] * other.data[13]
        result[14] =
            this.data[12] * other.data[2] + this.data[13] * other.data[6] + this.data[14] * other.data[10] + this.data[15] * other.data[14]
        result[15] =
            this.data[12] * other.data[3] + this.data[13] * other.data[7] + this.data[14] * other.data[11] + this.data[15] * other.data[15]

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

    fun multiply(other: Matrix4) = multiplyMatrices(this, other)

    fun preMultiply(other: Matrix4) = multiplyMatrices(other, this)

    operator fun timesAssign(other: Matrix4) {
        this.multiply(other)
    }

    /**
     * Multiply the two matrices [a] and [b] and store the result in this matrix
     *
     * @param a The first matrix
     * @param b The second matrix
     */
    fun multiplyMatrices(a: Matrix4, b: Matrix4): Matrix4 {
        val result0 =
            a.data[0] * b.data[0] + a.data[1] * b.data[4] + a.data[2] * b.data[8] + a.data[3] * b.data[12]
        val result1 =
            a.data[0] * b.data[1] + a.data[1] * b.data[5] + a.data[2] * b.data[9] + a.data[3] * b.data[13]
        val result2 =
            a.data[0] * b.data[2] + a.data[1] * b.data[6] + a.data[2] * b.data[10] + a.data[3] * b.data[14]
        val result3 =
            a.data[0] * b.data[3] + a.data[1] * b.data[7] + a.data[2] * b.data[11] + a.data[3] * b.data[15]

        val result4 =
            a.data[4] * b.data[0] + a.data[5] * b.data[4] + a.data[6] * b.data[8] + a.data[7] * b.data[12]
        val result5 =
            a.data[4] * b.data[1] + a.data[5] * b.data[5] + a.data[6] * b.data[9] + a.data[7] * b.data[13]
        val result6 =
            a.data[4] * b.data[2] + a.data[5] * b.data[6] + a.data[6] * b.data[10] + a.data[7] * b.data[14]
        val result7 =
            a.data[4] * b.data[3] + a.data[5] * b.data[7] + a.data[6] * b.data[11] + a.data[7] * b.data[15]

        val result8 =
            a.data[8] * b.data[0] + a.data[9] * b.data[4] + a.data[10] * b.data[8] + a.data[11] * b.data[12]
        val result9 =
            a.data[8] * b.data[1] + a.data[9] * b.data[5] + a.data[10] * b.data[9] + a.data[11] * b.data[13]
        val result10 =
            a.data[8] * b.data[2] + a.data[9] * b.data[6] + a.data[10] * b.data[10] + a.data[11] * b.data[14]
        val result11 =
            a.data[8] * b.data[3] + a.data[9] * b.data[7] + a.data[10] * b.data[11] + a.data[11] * b.data[15]

        val result12 =
            a.data[12] * b.data[0] + a.data[13] * b.data[4] + a.data[14] * b.data[8] + a.data[15] * b.data[12]
        val result13 =
            a.data[12] * b.data[1] + a.data[13] * b.data[5] + a.data[14] * b.data[9] + a.data[15] * b.data[13]
        val result14 =
            a.data[12] * b.data[2] + a.data[13] * b.data[6] + a.data[14] * b.data[10] + a.data[15] * b.data[14]
        val result15 =
            a.data[12] * b.data[3] + a.data[13] * b.data[7] + a.data[14] * b.data[11] + a.data[15] * b.data[15]

        this.data[0] = result0
        this.data[1] = result1
        this.data[2] = result2
        this.data[3] = result3

        this.data[4] = result4
        this.data[5] = result5
        this.data[6] = result6
        this.data[7] = result7

        this.data[8] = result8
        this.data[9] = result9
        this.data[10] = result10
        this.data[11] = result11

        this.data[12] = result12
        this.data[13] = result13
        this.data[14] = result14
        this.data[15] = result15

        return this
    }

    /**
     * Multiply this matrix with [other] and store the result in [result] array
     *
     * This method is used to avoid memory allocation. Note it's already in column major
     *
     * @param other The matrix to multiply
     * @param result The array to store the result
     * @param offset The offset of the array
     */
    fun multiplyToArray(other: Matrix4, result: FloatArray, offset: Int) {
        result[offset + 0] =
            this.data[0] * other.data[0] + this.data[1] * other.data[4] + this.data[2] * other.data[8] + this.data[3] * other.data[12]
        result[offset + 4] =
            this.data[0] * other.data[1] + this.data[1] * other.data[5] + this.data[2] * other.data[9] + this.data[3] * other.data[13]
        result[offset + 8] =
            this.data[0] * other.data[2] + this.data[1] * other.data[6] + this.data[2] * other.data[10] + this.data[3] * other.data[14]
        result[offset + 12] =
            this.data[0] * other.data[3] + this.data[1] * other.data[7] + this.data[2] * other.data[11] + this.data[3] * other.data[15]

        result[offset + 1] =
            this.data[4] * other.data[0] + this.data[5] * other.data[4] + this.data[6] * other.data[8] + this.data[7] * other.data[12]
        result[offset + 5] =
            this.data[4] * other.data[1] + this.data[5] * other.data[5] + this.data[6] * other.data[9] + this.data[7] * other.data[13]
        result[offset + 9] =
            this.data[4] * other.data[2] + this.data[5] * other.data[6] + this.data[6] * other.data[10] + this.data[7] * other.data[14]
        result[offset + 13] =
            this.data[4] * other.data[3] + this.data[5] * other.data[7] + this.data[6] * other.data[11] + this.data[7] * other.data[15]

        result[offset + 2] =
            this.data[8] * other.data[0] + this.data[9] * other.data[4] + this.data[10] * other.data[8] + this.data[11] * other.data[12]
        result[offset + 6] =
            this.data[8] * other.data[1] + this.data[9] * other.data[5] + this.data[10] * other.data[9] + this.data[11] * other.data[13]
        result[offset + 10] =
            this.data[8] * other.data[2] + this.data[9] * other.data[6] + this.data[10] * other.data[10] + this.data[11] * other.data[14]
        result[offset + 14] =
            this.data[8] * other.data[3] + this.data[9] * other.data[7] + this.data[10] * other.data[11] + this.data[11] * other.data[15]

        result[offset + 3] =
            this.data[12] * other.data[0] + this.data[13] * other.data[4] + this.data[14] * other.data[8] + this.data[15] * other.data[12]
        result[offset + 7] =
            this.data[12] * other.data[1] + this.data[13] * other.data[5] + this.data[14] * other.data[9] + this.data[15] * other.data[13]
        result[offset + 11] =
            this.data[12] * other.data[2] + this.data[13] * other.data[6] + this.data[14] * other.data[10] + this.data[15] * other.data[14]
        result[offset + 15] =
            this.data[12] * other.data[3] + this.data[13] * other.data[7] + this.data[14] * other.data[11] + this.data[15] * other.data[15]
    }

    fun multiplyToFloatBuffer(other: Matrix4, result: FloatBuffer) {
        result
            .put(this.data[0] * other.data[0] + this.data[1] * other.data[4] + this.data[2] * other.data[8] + this.data[3] * other.data[12])
            .put(this.data[4] * other.data[0] + this.data[5] * other.data[4] + this.data[6] * other.data[8] + this.data[7] * other.data[12])
            .put(this.data[8] * other.data[0] + this.data[9] * other.data[4] + this.data[10] * other.data[8] + this.data[11] * other.data[12])
            .put(this.data[12] * other.data[0] + this.data[13] * other.data[4] + this.data[14] * other.data[8] + this.data[15] * other.data[12])

        result
            .put(this.data[0] * other.data[1] + this.data[1] * other.data[5] + this.data[2] * other.data[9] + this.data[3] * other.data[13])
            .put(this.data[4] * other.data[1] + this.data[5] * other.data[5] + this.data[6] * other.data[9] + this.data[7] * other.data[13])
            .put( this.data[8] * other.data[1] + this.data[9] * other.data[5] + this.data[10] * other.data[9] + this.data[11] * other.data[13])
            .put(this.data[12] * other.data[1] + this.data[13] * other.data[5] + this.data[14] * other.data[9] + this.data[15] * other.data[13])

        result
            .put(this.data[0] * other.data[2] + this.data[1] * other.data[6] + this.data[2] * other.data[10] + this.data[3] * other.data[14])
            .put(this.data[4] * other.data[2] + this.data[5] * other.data[6] + this.data[6] * other.data[10] + this.data[7] * other.data[14])
            .put(this.data[8] * other.data[2] + this.data[9] * other.data[6] + this.data[10] * other.data[10] + this.data[11] * other.data[14])
            .put(this.data[12] * other.data[2] + this.data[13] * other.data[6] + this.data[14] * other.data[10] + this.data[15] * other.data[14])

        result
            .put(this.data[0] * other.data[3] + this.data[1] * other.data[7] + this.data[2] * other.data[11] + this.data[3] * other.data[15])
            .put(this.data[4] * other.data[3] + this.data[5] * other.data[7] + this.data[6] * other.data[11] + this.data[7] * other.data[15])
            .put(this.data[8] * other.data[3] + this.data[9] * other.data[7] + this.data[10] * other.data[11] + this.data[11] * other.data[15])
            .put(this.data[12] * other.data[3] + this.data[13] * other.data[7] + this.data[14] * other.data[11] + this.data[15] * other.data[15])
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
    
    fun inverseOf(other: Matrix4): Matrix4 {
        this[0] = other[5] * other[10] * other[15] -
                other[5] * other[11] * other[14] -
                other[9] * other[6] * other[15] +
                other[9] * other[7] * other[14] +
                other[13] * other[6] * other[11] -
                other[13] * other[7] * other[10]

        this[4] = -other[4] * other[10] * other[15] +
                other[4] * other[11] * other[14] +
                other[8] * other[6] * other[15] -
                other[8] * other[7] * other[14] -
                other[12] * other[6] * other[11] +
                other[12] * other[7] * other[10]

        this[8] = other[4] * other[9] * other[15] -
                other[4] * other[11] * other[13] -
                other[8] * other[5] * other[15] +
                other[8] * other[7] * other[13] +
                other[12] * other[5] * other[11] -
                other[12] * other[7] * other[9]

        this[12] = -other[4] * other[9] * other[14] +
                other[4] * other[10] * other[13] +
                other[8] * other[5] * other[14] -
                other[8] * other[6] * other[13] -
                other[12] * other[5] * other[10] +
                other[12] * other[6] * other[9]

        this[1] = -other[1] * other[10] * other[15] +
                other[1] * other[11] * other[14] +
                other[9] * other[2] * other[15] -
                other[9] * other[3] * other[14] -
                other[13] * other[2] * other[11] +
                other[13] * other[3] * other[10]

        this[5] = other[0] * other[10] * other[15] -
                other[0] * other[11] * other[14] -
                other[8] * other[2] * other[15] +
                other[8] * other[3] * other[14] +
                other[12] * other[2] * other[11] -
                other[12] * other[3] * other[10]

        this[9] = -other[0] * other[9] * other[15] +
                other[0] * other[11] * other[13] +
                other[8] * other[1] * other[15] -
                other[8] * other[3] * other[13] -
                other[12] * other[1] * other[11] +
                other[12] * other[3] * other[9]

        this[13] = other[0] * other[9] * other[14] -
                other[0] * other[10] * other[13] -
                other[8] * other[1] * other[14] +
                other[8] * other[2] * other[13] +
                other[12] * other[1] * other[10] -
                other[12] * other[2] * other[9]

        this[2] = other[1] * other[6] * other[15] -
                other[1] * other[7] * other[14] -
                other[5] * other[2] * other[15] +
                other[5] * other[3] * other[14] +
                other[13] * other[2] * other[7] -
                other[13] * other[3] * other[6]

        this[6] = -other[0] * other[6] * other[15] +
                other[0] * other[7] * other[14] +
                other[4] * other[2] * other[15] -
                other[4] * other[3] * other[14] -
                other[12] * other[2] * other[7] +
                other[12] * other[3] * other[6]

        this[10] = other[0] * other[5] * other[15] -
                other[0] * other[7] * other[13] -
                other[4] * other[1] * other[15] +
                other[4] * other[3] * other[13] +
                other[12] * other[1] * other[7] -
                other[12] * other[3] * other[5]

        this[14] = -other[0] * other[5] * other[14] +
                other[0] * other[6] * other[13] +
                other[4] * other[1] * other[14] -
                other[4] * other[2] * other[13] -
                other[12] * other[1] * other[6] +
                other[12] * other[2] * other[5]

        this[3] = -other[1] * other[6] * other[11] +
                other[1] * other[7] * other[10] +
                other[5] * other[2] * other[11] -
                other[5] * other[3] * other[10] -
                other[9] * other[2] * other[7] +
                other[9] * other[3] * other[6]

        this[7] = other[0] * other[6] * other[11] -
                other[0] * other[7] * other[10] -
                other[4] * other[2] * other[11] +
                other[4] * other[3] * other[10] +
                other[8] * other[2] * other[7] -
                other[8] * other[3] * other[6]

        this[11] = -other[0] * other[5] * other[11] +
                other[0] * other[7] * other[9] +
                other[4] * other[1] * other[11] -
                other[4] * other[3] * other[9] -
                other[8] * other[1] * other[7] +
                other[8] * other[3] * other[5]

        this[15] = other[0] * other[5] * other[10] -
                other[0] * other[6] * other[9] -
                other[4] * other[1] * other[10] +
                other[4] * other[2] * other[9] +
                other[8] * other[1] * other[6] -
                other[8] * other[2] * other[5]

        val det = other[0] * this[0] + other[1] * this[4] + other[2] * this[8] + other[3] * this[12]

        if (det == 0f) {
            throw IllegalStateException("Matrix is singular and cannot be inverted")
        }

        val invDet = 1.0f / det
        for (i in 0..15) {
            this[i] *= invDet
        }

        return this
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

