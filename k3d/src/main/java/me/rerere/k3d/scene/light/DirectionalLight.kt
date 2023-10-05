package me.rerere.k3d.scene.light

import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Vec3

class DirectionalLight(
    intensity: Float = 1f,
    color: Color,
    target: Vec3,
) : Light(intensity, color) {
    val target: Vec3 = target.copy()

    override fun isDirty(): Boolean {
        return super.isDirty() || target.isDirty()
    }

    override fun clearDirty() {
        super.clearDirty()
        target.clearDirty()
    }
}