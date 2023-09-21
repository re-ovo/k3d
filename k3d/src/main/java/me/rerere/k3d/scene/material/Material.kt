package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import kotlin.reflect.KProperty

open class ShaderMaterial(
    val program: ShaderProgramSource,
    val uniforms: MutableMap<String, Uniform> = mutableMapOf(),
    val textures: MutableMap<String, Texture> = mutableMapOf()
) {
    fun uniformOf(name: String): MaterialUniformDelegate {
        return MaterialUniformDelegate(this, name)
    }

    fun uniformOf(type: BuiltInUniformName) = uniformOf(type.name)

    fun textureOf(name: String): MaterialTextureDelegate {
        return MaterialTextureDelegate(this, name)
    }

    fun textureOf(type: BuiltInUniformName) = textureOf(type.name)

    fun getUniform(name: String): Uniform? {
        return uniforms[name]
    }

    fun setUniform(name: String, uniform: Uniform?) {
        if (uniform == null) {
            uniforms.remove(name)
            program.removeMarcoDefinition("HAS_UNIFORM_$name")
        } else {
            uniforms[name] = uniform
            program.addMarcoDefinition("HAS_UNIFORM_$name")
        }
    }

    fun getTexture(name: String): Texture? {
        return textures[name]
    }

    fun setTexture(name: String, texture: Texture?) {
        if (texture == null) {
            textures.remove(name)
            program.removeMarcoDefinition("HAS_TEXTURE_$name")
        } else {
            textures[name] = texture
            program.addMarcoDefinition("HAS_TEXTURE_$name")
        }
    }
}

class MaterialUniformDelegate(private val material: ShaderMaterial, private val name: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Uniform? {
        return material.getUniform(name)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Uniform?) {
        material.setUniform(name, value)
    }
}

class MaterialTextureDelegate(private val material: ShaderMaterial, private val name: String) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Texture? {
        return material.getTexture(name)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Texture?) {
        material.setTexture(name, value)
    }
}