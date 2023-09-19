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

data class Color4f(
    val r: Float,
    val g: Float,
    val b: Float,
    val a: Float = 1f
) {
    companion object {
        @JvmStatic
        fun fromHex(hex: String): Color4f {
            val value = hex.hexToInt(hexColorFormat)
            return Color4f(
                ((value shr 16) and 0xFF) / 255f,
                ((value shr 8) and 0xFF) / 255f,
                (value and 0xFF) / 255f,
                ((value shr 24) and 0xFF) / 255f
            )
        }
    }

    fun toLinear(): Color4f {
        return Color4f(
            r = r.srgbToLinear(),
            g = g.srgbToLinear(),
            b = b.srgbToLinear(),
            a = a
        )
    }

    fun toSRGB(): Color4f {
        return Color4f(
            r = r.linearToSRGB(),
            g = g.linearToSRGB(),
            b = b.linearToSRGB(),
            a = a
        )
    }
}

data class Color3f(
    val r: Float,
    val g: Float,
    val b: Float
) {
    companion object {
        @JvmStatic
        fun fromHex(hex: String): Color3f {
            val value = hex.hexToInt(hexColorFormat)
            return Color3f(
                ((value shr 16) and 0xFF) / 255f,
                ((value shr 8) and 0xFF) / 255f,
                (value and 0xFF) / 255f
            )
        }
    }

    fun toLinear(): Color3f {
        return Color3f(
            r = r.srgbToLinear(),
            g = g.srgbToLinear(),
            b = b.srgbToLinear()
        )
    }

    fun toSRGB(): Color3f {
        return Color3f(
            r = r.linearToSRGB(),
            g = g.linearToSRGB(),
            b = b.linearToSRGB()
        )
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