package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.shader.ShaderProgram
import me.rerere.k3d.renderer.shader.Uniform

open class ShaderMaterial(
    val program: ShaderProgram,
    val uniforms: MutableSet<Uniform> = hashSetOf(),
    val textures: MutableMap<String, Texture> = mutableMapOf()
)