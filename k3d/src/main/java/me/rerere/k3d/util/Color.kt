@file:OptIn(ExperimentalStdlibApi::class)

package me.rerere.k3d.util

import kotlin.math.pow

private val hexColorFormat by lazy {
    HexFormat {
        number {
            prefix = "#"
        }
    }
}

enum class ColorSpace {
    SRGB,
    LINEAR_SRGB
}

data class Color(
    var r: Float,
    var g: Float,
    var b: Float,
    var a: Float = 1f,
) {
    init {
        require(r in 0f..1f)
        require(g in 0f..1f)
        require(b in 0f..1f)
        require(a in 0f..1f)
    }

    companion object {
        @JvmStatic
        fun fromRGBAHex(hex: String): Color {
            val value = hex.hexToInt(hexColorFormat)
            val r = ((value shr 16) and 0xFF) / 255f
            val g = ((value shr 8) and 0xFF) / 255f
            val b = (value and 0xFF) / 255f
            val a = ((value shr 24) and 0xFF) / 255f
            return Color(r.srgbToLinear(), g.srgbToLinear(), b.srgbToLinear(), a)
        }

        @JvmStatic
        fun fromRGBHex(hex: String): Color {
            val value = hex.hexToInt(hexColorFormat)
            val r = ((value shr 16) and 0xFF) / 255f
            val g = ((value shr 8) and 0xFF) / 255f
            val b = (value and 0xFF) / 255f
            return Color(r.srgbToLinear(), g.srgbToLinear(), b.srgbToLinear(), 1f)
        }

        @JvmStatic
        fun white() = Color(1f, 1f, 1f)

        @JvmStatic
        fun black() = Color(0f, 0f, 0f)
    }
}

internal fun Float.srgbToLinear(): Float {
    return if (this <= 0.04045f) {
        this / 12.92f
    } else {
        ((this + 0.055f) / 1.055f).pow(2.4f)
    }
}

internal fun Float.linearToSRGB(): Float {
    return if (this <= 0.0031308f) {
        this * 12.92f
    } else {
        1.055f * this.pow(1f / 2.4f) - 0.055f
    }
}