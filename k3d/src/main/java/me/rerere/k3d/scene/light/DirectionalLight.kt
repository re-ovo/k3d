package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color3f
import me.rerere.k3d.util.math.Vec3

class DirectionalLight(
    intensity: Float = 1f,
    color: Color3f,
    val target: Vec3 = Vec3(0f, 0f, 0f)
) : Light(intensity, color)