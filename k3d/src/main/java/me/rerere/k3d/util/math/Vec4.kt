package me.rerere.k3d.util.math

import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.dirtyFloatValue

class Vec4(
    x: Float = 0.0f,
    y: Float = 0.0f,
    z: Float = 0.0f,
    w: Float = 0.0f,
) : Dirty {
    var x by dirtyFloatValue(x)
    var y by dirtyFloatValue(y)
    var z by dirtyFloatValue(z)
    var w by dirtyFloatValue(w)

    operator fun get(index: Int): Float {
        return when(index) {
            0 -> x
            1 -> y
            2 -> z
            3 -> w
            else -> error("Invalid index: $index")
        }
    }

    override fun toString(): String {
        return "Vec4(x=$x, y=$y, z=$z, w=$w)"
    }
}