package me.rerere.k3d.scene.light

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.Color

abstract class Light(
    var intensity: Float = 1f,
    var color: Color,
) : Actor()