package me.rerere.k3d.scene.animation

interface Interpolation {
    fun interpolate(t: Float, t0: Float, t1: Float, v0: Float, v1: Float): Float

    companion object {
        @JvmStatic
        fun fromString(name: String): Interpolation {
            return when (name) {
                "LINEAR" -> LinearInterpolation
                "STEP" -> StepInterpolation
                "CUBICSPLINE" -> throw NotImplementedError("Cubic spline interpolation is not implemented yet")
                else -> throw IllegalArgumentException("Unknown interpolation type: $name")
            }
        }
    }
}

object LinearInterpolation : Interpolation {
    override fun interpolate(t: Float, t0: Float, t1: Float, v0: Float, v1: Float): Float {
        if (t <= t0) return v0
        if (t >= t1) return v1
        return v0 + (v1 - v0) * ((t - t0) / (t1 - t0))
    }
}

object StepInterpolation : Interpolation {
    override fun interpolate(t: Float, t0: Float, t1: Float, v0: Float, v1: Float): Float {
        return if (t >= t1) v1 else v0
    }
}