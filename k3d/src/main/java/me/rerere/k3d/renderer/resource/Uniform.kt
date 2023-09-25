package me.rerere.k3d.renderer.resource

import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.Vec4

/**
 * This class represents a single uniform
 */
sealed class Uniform {
    class Float(var value: kotlin.Float) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Float) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): kotlin.Int {
            return value.hashCode()
        }
    }

    class Int(var value: kotlin.Int) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Int) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): kotlin.Int {
            return value
        }
    }

    class Vec3f(var value: Vec3) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Vec3f) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): kotlin.Int {
            return value.hashCode()
        }
    }

    class Vec4f(var value: Vec4) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Vec4f) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): kotlin.Int {
            return value.hashCode()
        }
    }

    class Mat4(var value: Matrix4, var transpose: Boolean) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Mat4) return false

            if (value != other.value) return false
            if (transpose != other.transpose) return false

            return true
        }

        override fun hashCode(): kotlin.Int {
            var result = value.hashCode()
            result = 31 * result + transpose.hashCode()
            return result
        }
    }
}