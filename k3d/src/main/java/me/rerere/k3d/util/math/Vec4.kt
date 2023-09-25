package me.rerere.k3d.util.math

import me.rerere.k3d.util.Dirty

class Vec4(
    x: Float = 0.0f,
    y: Float = 0.0f,
    z: Float = 0.0f,
    w: Float = 0.0f,
) : Dirty {
    var x = x
        set(value) {
            field = value
            markDirty()
        }
    var y = y
        set(value) {
            field = value
            markDirty()
        }
    var z = z
        set(value) {
            field = value
            markDirty()
        }
    var w = w
        set(value) {
            field = value
            markDirty()
        }

    override var dirty: Boolean = false
}