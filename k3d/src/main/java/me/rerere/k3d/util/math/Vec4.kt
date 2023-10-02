package me.rerere.k3d.util.math

import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.dirtyValue

class Vec4(
    x: Float = 0.0f,
    y: Float = 0.0f,
    z: Float = 0.0f,
    w: Float = 0.0f,
) : Dirty {
    var x by dirtyValue(x)
    var y by dirtyValue(y)
    var z by dirtyValue(z)
    var w by dirtyValue(w)

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