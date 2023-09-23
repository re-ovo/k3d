package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import kotlin.reflect.KProperty

/**
 * The material of a [Primitive][me.rerere.k3d.scene.actor.Primitive]
 *
 * If you want to define a new material, you can extend this class, like:
 * ```
 * class MyMaterial : ShaderMaterial(yourShaderProgramSource) {
 *  var myUniform by uniformOf("myUniform")
 *  var myTexture by textureOf("myTexture")
 *}
 * ```
 *
 * @property program The shader program source
 * @property uniforms The uniforms of this material
 * @property textures The textures of this material
 * @constructor Create empty Shader material
 */
open class ShaderMaterial(
    val program: ShaderProgramSource,
    val uniforms: MutableMap<String, Uniform> = mutableMapOf(),
    val textures: MutableMap<String, Texture> = mutableMapOf()
) {
    fun <T : Uniform> uniformOf(name: String, def: T): MaterialUniformDelegate<T> {
        if(!uniforms.containsKey(name)){
            uniforms[name] = def
        }
        return MaterialUniformDelegate(this, name, def)
    }

    fun <T : Uniform> uniformOf(type: BuiltInUniformName, def: T) = uniformOf(type.uniformName, def)

    fun textureOf(name: String): MaterialTextureDelegate {
        return MaterialTextureDelegate(this, name)
    }

    fun textureOf(type: BuiltInUniformName) = textureOf(type.uniformName)

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

class MaterialUniformDelegate<T : Uniform>(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: T
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return material.getUniform(name) as? T ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
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