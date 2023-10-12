package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.Vec4
import me.rerere.k3d.util.system.Disposable

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
    val uniforms: MutableList<Pair<String, Uniform>> = arrayListOf(),
    val textures: MutableList<Pair<String, Texture>> = arrayListOf(),
) : Disposable {
    var name = ""

    var alphaMode = AlphaMode.OPAQUE
    var alphaCutoff by floatUniformOf(BuiltInUniformName.ALPHA_CUTOFF, 0.5f)
    var doubleSided = false

    fun getUniform(name: String): Uniform? {
        return uniforms.find { it.first == name }?.second
    }

    fun setUniform(name: String, uniform: Uniform?) {
        if (uniform == null) {
            uniforms.removeIf { it.first == name }
            program.removeMarcoDefinition("HAS_UNIFORM_$name")
        } else {
            uniforms.removeIf { it.first == name }
            uniforms.add(name to uniform)
            program.addMarcoDefinition("HAS_UNIFORM_$name")
        }
    }

    fun getTexture(name: String): Texture? {
        return textures.find { it.first == name }?.second
    }

    fun setTexture(name: String, texture: Texture?) {
        if (texture == null) {
            textures.removeIf { it.first == name }
            program.removeMarcoDefinition("HAS_TEXTURE_$name")
        } else {
            textures.removeIf { it.first == name }
            textures.add(name to texture)
            program.addMarcoDefinition("HAS_TEXTURE_$name")
        }
    }

    fun textureOf(name: String): MaterialTextureDelegate {
        return MaterialTextureDelegate(this, name)
    }

    fun textureOf(type: BuiltInUniformName) = textureOf(type.uniformName)

    fun floatUniformOf(name: String, def: Float = 0f): MaterialUniformFloatDelegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Float(def)
        }
        return MaterialUniformFloatDelegate(this, name, def)
    }

    fun floatUniformOf(type: BuiltInUniformName, def: Float = 0f) =
        floatUniformOf(type.uniformName, def)

    fun intUniformOf(name: String, def: Int = 0): MaterialUniformIntDelegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Int(def)
        }
        return MaterialUniformIntDelegate(this, name, def)
    }

    fun intUniformOf(type: BuiltInUniformName, def: Int = 0) = intUniformOf(type.uniformName, def)

    fun vec3UniformOf(name: String, def: Vec3 = Vec3()): MaterialUniformVec3Delegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Vec3f(def)
        }
        return MaterialUniformVec3Delegate(this, name, def)
    }

    fun vec3UniformOf(type: BuiltInUniformName, def: Vec3 = Vec3()) =
        vec3UniformOf(type.uniformName, def)

    fun mat4UniformOf(
        name: String,
        def: Matrix4 = Matrix4.identity()
    ): MaterialUniformMat4Delegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Mat4(def, true)
        }
        return MaterialUniformMat4Delegate(this, name, def)
    }

    fun mat4UniformOf(type: BuiltInUniformName, def: Matrix4 = Matrix4.identity()) =
        mat4UniformOf(type.uniformName, def)

    fun color3fUniformOf(
        name: String,
        def: Color = Color.fromRGBHex("#FFFFFF")
    ): MaterialUniformColor3fDelegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Vec3f(Vec3(def.r, def.g, def.b))
        }
        return MaterialUniformColor3fDelegate(this, name, def)
    }

    fun color3fUniformOf(type: BuiltInUniformName, def: Color = Color.fromRGBHex("#FFFFFF")) =
        color3fUniformOf(type.uniformName, def)

    fun color4fUniformOf(
        name: String,
        def: Color = Color.fromRGBHex("#FFFFFF")
    ): MaterialUniformColor4fDelegate {
        if(getUniform(name) == null){
            uniforms += name to Uniform.Vec4f(Vec4(def.r, def.g, def.b, def.a))
        }
        return MaterialUniformColor4fDelegate(this, name, def)
    }

    fun color4fUniformOf(type: BuiltInUniformName, def: Color = Color.fromRGBHex("#FFFFFF")) =
        color4fUniformOf(type.uniformName, def)

    override fun dispose() {}
}