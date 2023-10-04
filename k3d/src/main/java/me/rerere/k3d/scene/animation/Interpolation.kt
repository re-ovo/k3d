package me.rerere.k3d.scene.animation

interface Interpolation {
    fun interpolate(t: Float, t0: Float, t1: Float, v0: FloatArray, v1: FloatArray): FloatArray

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
    override fun interpolate(t: Float, t0: Float, t1: Float, v0: FloatArray, v1: FloatArray): FloatArray {
        val a = (t - t0) / (t1 - t0)
        return v0.mapIndexed { index, v ->
            v * (1 - a) + v1[index] * a
        }.toFloatArray()
    }
}

object StepInterpolation : Interpolation {
    override fun interpolate(t: Float, t0: Float, t1: Float, v0: FloatArray, v1: FloatArray): FloatArray {
        return v0
    }
}