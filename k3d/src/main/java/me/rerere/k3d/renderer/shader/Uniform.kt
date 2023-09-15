package me.rerere.k3d.renderer.shader

sealed class Uniform {
    abstract val name: String

    class Float1(override val name: String, var value: Float) : Uniform()
    class Int1(override val name: String, var value: Int) : Uniform()
    class Vec3f(override val name: String, var x: Float, var y: Float, var z: Float) : Uniform()
    class Vec4f(override val name: String, var x: Float, var y: Float, var z: Float, var w: Float) : Uniform()
    class Mat4(override val name: String, var value: FloatArray, var transpose: Boolean) : Uniform()

    override fun toString(): String {
        return "Uniform(name='$name')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Uniform) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}