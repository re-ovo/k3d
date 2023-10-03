package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.system.dependsOn

class DirectionalLight(
    intensity: Float = 1f,
    color: Color,
    target: Vec3,
) : Light(intensity, color) {
    val target = target.copy()

    init {
        this.dependsOn(target)
    }
}