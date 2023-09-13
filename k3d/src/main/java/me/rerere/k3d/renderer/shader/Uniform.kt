package me.rerere.k3d.renderer.shader

sealed class Uniform {
    abstract val name: String

    class Float(override val name: String, var value: kotlin.Float) : Uniform()
    class Int(override val name: String, var value: kotlin.Int) : Uniform()
    class Vec3(override val name: String, var x: kotlin.Float, var y: kotlin.Float, var z: kotlin.Float) : Uniform()
    class Vec4(override val name: String, var x: kotlin.Float, var y: kotlin.Float, var z: kotlin.Float, var w: kotlin.Float) : Uniform()
    class Mat4(override val name: String, var value: FloatArray) : Uniform()
}