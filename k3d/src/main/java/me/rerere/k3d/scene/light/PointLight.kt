package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color3f

class PointLight(
    intensity: Float = 1f,
    color: Color3f,
    val distance: Float = 0f,
    val decay: Float = 2f
) : Light(intensity, color)