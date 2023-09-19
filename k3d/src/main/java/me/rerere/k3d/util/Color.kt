package me.rerere.k3d.util

enum class ColorSpace {
    SRGB,
    LINEAR_SRGB
}

data class Color4f(
    val r: Float,
    val g: Float,
    val b: Float,
    val a: Float = 1f
)

data class Color3f(
    val r: Float,
    val g: Float,
    val b: Float
)

