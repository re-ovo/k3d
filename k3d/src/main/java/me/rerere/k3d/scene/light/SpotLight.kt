package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.toRadian

class SpotLight(
    intensity: Float = 1f,
    color: Color,
    var target: Vec3,
    var angle: Float = 90f.toRadian(),
    var penumbra: Float = 0f, // 0 ~ 1
) : Light(intensity, color)