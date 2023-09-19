package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color4f

class PointLight(
    intensity: Float = 1f,
    color: Color4f,
    val distance: Float = 0f,
    val decay: Float = 2f
) : LightActor(intensity, color)