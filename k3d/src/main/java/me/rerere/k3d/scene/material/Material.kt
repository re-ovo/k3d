package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.Vec4

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
    var alphaMode = AlphaMode.OPAQUE
    var alphaCutoff by floatUniformOf(BuiltInUniformName.ALPHA_CUTOFF, 0.5f)
    var doubleSided = false

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