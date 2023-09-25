package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.ColorSpace
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.Vec4
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
    fun textureOf(name: String): MaterialTextureDelegate {
        return MaterialTextureDelegate(this, name)
    }

    fun textureOf(type: BuiltInUniformName) = textureOf(type.uniformName)

    fun floatUniformOf(name: String, def: Float = 0f): MaterialUniformFloatDelegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Float(def)
        }
        return MaterialUniformFloatDelegate(this, name, def)
    }

    fun floatUniformOf(type: BuiltInUniformName, def: Float = 0f) =
        floatUniformOf(type.uniformName, def)

    fun intUniformOf(name: String, def: Int = 0): MaterialUniformIntDelegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Int(def)
        }
        return MaterialUniformIntDelegate(this, name, def)
    }

    fun intUniformOf(type: BuiltInUniformName, def: Int = 0) = intUniformOf(type.uniformName, def)

    fun vec3UniformOf(name: String, def: Vec3 = Vec3()): MaterialUniformVec3Delegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Vec3f(def)
        }
        return MaterialUniformVec3Delegate(this, name, def)
    }

    fun vec3UniformOf(type: BuiltInUniformName, def: Vec3 = Vec3()) =
        vec3UniformOf(type.uniformName, def)

    fun mat4UniformOf(
        name: String,
        def: Matrix4 = Matrix4.identity()
    ): MaterialUniformMat4Delegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Mat4(def, true)
        }
        return MaterialUniformMat4Delegate(this, name, def)
    }

    fun mat4UniformOf(type: BuiltInUniformName, def: Matrix4 = Matrix4.identity()) =
        mat4UniformOf(type.uniformName, def)

    fun color3fUniformOf(
        name: String,
        def: Color = Color.fromRGBHex("#FFFFFF")
    ): MaterialUniformColor3fDelegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Vec3f(Vec3(def.r, def.g, def.b))
        }
        return MaterialUniformColor3fDelegate(this, name, def)
    }

    fun color3fUniformOf(type: BuiltInUniformName, def: Color = Color.fromRGBHex("#FFFFFF")) =
        color3fUniformOf(type.uniformName, def)

    fun color4fUniformOf(
        name: String,
        def: Color = Color.fromRGBHex("#FFFFFF")
    ): MaterialUniformColor4fDelegate {
        if(!uniforms.containsKey(name)){
            uniforms[name] = Uniform.Vec4f(Vec4(def.r, def.g, def.b, def.a))
        }
        return MaterialUniformColor4fDelegate(this, name, def)
    }

    fun color4fUniformOf(type: BuiltInUniformName, def: Color = Color.fromRGBHex("#FFFFFF")) =
        color4fUniformOf(type.uniformName, def)

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

class MaterialUniformFloatDelegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Float
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Float {
        return (material.getUniform(name) as? Uniform.Float)?.value ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
        material.setUniform(name, Uniform.Float(value))
    }
}

class MaterialUniformIntDelegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Int
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Int {
        return (material.getUniform(name) as? Uniform.Int)?.value ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        material.setUniform(name, Uniform.Int(value))
    }
}

class MaterialUniformVec3Delegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Vec3
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Vec3 {
        return (material.getUniform(name) as? Uniform.Vec3f)?.value ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Vec3) {
        material.setUniform(name, Uniform.Vec3f(value))
    }
}

class MaterialUniformMat4Delegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Matrix4
) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Matrix4 {
        return (material.getUniform(name) as? Uniform.Mat4)?.value ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Matrix4) {
        material.setUniform(name, Uniform.Mat4(value, true))
    }
}

class MaterialUniformColor3fDelegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Color
) {
    private fun Vec3.toColor(): Color {
        return Color(r = this.x, g = this.y, b = this.z, a = 1f)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Color {
        return (material.getUniform(name) as? Uniform.Vec3f)?.value?.toColor() ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Color) {
        material.setUniform(
            name, Uniform.Vec3f(
                Vec3(
                    value.r,
                    value.g,
                    value.b
                )
            )
        )
    }
}

class MaterialUniformColor4fDelegate(
    private val material: ShaderMaterial,
    private val name: String,
    private val def: Color
) {
    private fun Vec4.toColor(): Color {
        return Color(r = this.x, g = this.y, b = this.z, a = 1f)
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Color {
        return (material.getUniform(name) as? Uniform.Vec4f)?.value?.toColor() ?: def
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Color) {
        material.setUniform(
            name, Uniform.Vec4f(
                Vec4(
                    value.r,
                    value.g,
                    value.b,
                    value.a
                )
            )
        )
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