package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color

class PointLight(
    intensity: Float = 1f,
    color: Color,
    var constant: Float = 1f,
    var linear: Float = 0.09f,
    var quadratic: Float = 0.032f
) : Light(intensity, color)