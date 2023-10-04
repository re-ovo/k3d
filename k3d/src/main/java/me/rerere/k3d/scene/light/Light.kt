package me.rerere.k3d.scene.light

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.system.dirtyFloatValue
import me.rerere.k3d.util.system.dirtyValue

abstract class Light(
    intensity: Float = 1f,
    color: Color,
) : Actor() {
    var intensity by dirtyFloatValue(intensity)
    var color by dirtyValue(color)
}