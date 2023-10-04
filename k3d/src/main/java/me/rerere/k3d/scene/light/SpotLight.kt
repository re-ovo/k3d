package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.rotation.toRadian
import me.rerere.k3d.util.system.dependsOn
import me.rerere.k3d.util.system.dirtyFloatValue
import me.rerere.k3d.util.system.dirtyValue

class SpotLight(
    intensity: Float = 1f,
    color: Color,
    target: Vec3,
    angle: Float = 90f.toRadian(),
    penumbra: Float = 0f, // 0 ~ 1
) : Light(intensity, color) {
    var angle by dirtyFloatValue(angle)
    var penumbra by dirtyFloatValue(penumbra)
    val target = target.copy()

    init {
        this.dependsOn(target)
    }
}