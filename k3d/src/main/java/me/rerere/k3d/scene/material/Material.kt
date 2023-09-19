package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.renderer.resource.Uniform

open class ShaderMaterial(
    val program: ShaderProgramSource,
    val uniforms: MutableMap<String, Uniform> = mutableMapOf(),
    val textures: MutableMap<String, Texture> = mutableMapOf()
)