package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color
import me.rerere.k3d.util.system.dirtyValue

class PointLight(
    intensity: Float = 1f,
    color: Color,
    constant: Float = 1f,
    linear: Float = 0.09f,
    quadratic: Float = 0.032f
) : Light(intensity, color) {
    var constant by dirtyValue(constant)
    var linear by dirtyValue(linear)
    var quadratic by dirtyValue(quadratic)
}