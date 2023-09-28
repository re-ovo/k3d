package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.util.Color
import me.rerere.k3d.util.math.Matrix4
import me.rerere.k3d.util.math.Vec3
import me.rerere.k3d.util.math.Vec4
import kotlin.reflect.KProperty

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