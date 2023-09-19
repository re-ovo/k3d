package me.rerere.k3d.scene.light

import me.rerere.k3d.scene.actor.Actor
import me.rerere.k3d.util.Color3f

abstract class LightActor(
    var intensity: Float = 1f,
    var color: Color3f,
) : Actor()