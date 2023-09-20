package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color3f
import me.rerere.k3d.util.math.rotation.toRadian

class SpotLight(
    intensity: Float = 1f,
    color: Color3f,
    val angle: Float = 90f.toRadian(),
    val penumbra: Float = 0f, // 0 ~ 1
    val distance: Float = 0f,
    val decay: Float = 2f,
) : Light(intensity, color)