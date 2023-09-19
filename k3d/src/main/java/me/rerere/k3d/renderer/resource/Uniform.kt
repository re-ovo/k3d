package me.rerere.k3d.renderer.resource

/**
 * This class represents a single uniform
 */
sealed class Uniform {
    class Float1(var value: Float) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Float1) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }
    }

    class Int1(var value: Int) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Int1) return false

            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            return value
        }
    }

    class Vec3f(var x: Float, var y: Float, var z: Float) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Vec3f) return false

            if (x != other.x) return false
            if (y != other.y) return false
            if (z != other.z) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + z.hashCode()
            return result
        }
    }

    class Vec4f(var x: Float, var y: Float, var z: Float, var w: Float) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Vec4f) return false

            if (x != other.x) return false
            if (y != other.y) return false
            if (z != other.z) return false
            if (w != other.w) return false

            return true
        }

        override fun hashCode(): Int {
            var result = x.hashCode()
            result = 31 * result + y.hashCode()
            result = 31 * result + z.hashCode()
            result = 31 * result + w.hashCode()
            return result
        }
    }

    class Mat4(var value: FloatArray, var transpose: Boolean) : Uniform() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Mat4) return false

            if (!value.contentEquals(other.value)) return false
            if (transpose != other.transpose) return false

            return true
        }

        override fun hashCode(): Int {
            var result = value.contentHashCode()
            result = 31 * result + transpose.hashCode()
            return result
        }
    }
}