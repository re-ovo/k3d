package me.rerere.k3d.scene.light

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.system.dirtyFloatValue
import me.rerere.k3d.util.system.dirtyValue

abstract class Light(
    intensity: Float = 1f,
    color: Color,
) : Actor() {
    private var _dirty = false

    var intensity by dirtyFloatValue(intensity)
    var color by dirtyValue(color)

    override fun markDirtyNew() {
        _dirty = true
    }

    override fun clearDirty() {
        _dirty = false
        super.clearDirty()
    }

    override fun isDirty(): Boolean {
        return _dirty || super.isDirty()
    }
}