package me.rerere.k3d.renderer.shader

import me.rerere.k3d.util.Dirty

class ShaderProgramSource(
    vertexShader: String,
    fragmentShader: String,
    marcoDefinitions: Set<MarcoDefinition> = emptySet()
) : Dirty {
    var vertexShader: String = vertexShader
        set(value) {
            field = value
            markDirty()
        }
    var fragmentShader: String = fragmentShader
        set(value) {
            field = value
            markDirty()
        }
    private val _marcoDefinitions: MutableSet<MarcoDefinition> = marcoDefinitions.toMutableSet()
    val marcoDefinitions: Set<MarcoDefinition> = _marcoDefinitions

    fun addMarcoDefinition(name: String, value: String? = null) {
        _marcoDefinitions.add(MarcoDefinition(name, value))
        markDirty()
    }

    fun removeMarcoDefinition(name: String) {
        _marcoDefinitions.removeIf {
            it.name == name
        }
        markDirty()
    }

    init {
        require(vertexShader.isNotBlank() && fragmentShader.isNotBlank()) {
            "Shader source cannot be blank"
        }
        // println(vertexShader.trimIndent())
        // println(fragmentShader.trimIndent())
    }

    override var dirty: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShaderProgramSource) return false

        if (vertexShader != other.vertexShader) return false
        if (fragmentShader != other.fragmentShader) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertexShader.hashCode()
        result = 31 * result + fragmentShader.hashCode()
        return result
    }
}

class MarcoDefinition(
    val name: String,
    val value: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarcoDefinition) return false
        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

enum class BuiltInAttributeName(val attributeName: String) {
    POSITION("a_pos"),
    NORMAL("a_normal"),
    TEXCOORD_BASE("a_texCoordBase"),
    TEXCOORD_NORMAL("a_texCoordNormal"),
    TEXCOORD_METALLIC("a_texCoordMetallic"),
    TEXCOORD_ROUGHNESS("a_texCoordRoughness"),
    TEXCOORD_OCCLUSION("a_texCoordOcclusion"),
    TEXCOORD_EMISSIVE("a_texCoordEmissive"),
    COLOR("a_color"), // vertex color
    TANGENT("a_tangent"),
    BITANGENT("a_bitangent")
}

enum class BuiltInUniformName(val uniformName: String) {
    MODEL_MATRIX("u_modelMatrix"),
    VIEW_MATRIX("u_viewMatrix"),
    PROJECTION_MATRIX("u_projectionMatrix"),

    TEXTURE_BASE("u_textureBase"),
    TEXTURE_NORMAL("u_textureNormal"),
    TEXTURE_METALLIC("u_textureMetallic"),
    TEXTURE_ROUGHNESS("u_textureRoughness"),
    TEXTURE_OCCLUSION("u_textureOcclusion"),
    TEXTURE_EMISSIVE("u_textureEmissive"),

    MATERIAL_ROUGHNESS("u_materialRoughness"),
    MATERIAL_METALLIC("u_materialMetallic"),
    MATERIAL_COLOR("u_materialColor"),
    MATERIAL_EMISSIVE("u_materialEmissive"),

    CAMERA_POSITION("u_cameraPos"),
    ALPHA_CUTOFF("u_alphaCutoff"),
    POINT_SIZE("u_pointSize"),
}