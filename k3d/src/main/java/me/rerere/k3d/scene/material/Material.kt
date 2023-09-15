package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.shader.ShaderProgram
import me.rerere.k3d.renderer.shader.Uniform

open class RawShaderMaterial(
    val program: ShaderProgram,
    val uniforms: Set<Uniform>
)

open class ShaderMaterial(
    program: ShaderProgram,
    uniforms: Set<Uniform>
) : RawShaderMaterial(program, uniforms)