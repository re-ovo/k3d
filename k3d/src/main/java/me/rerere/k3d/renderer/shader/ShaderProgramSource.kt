package me.rerere.k3d.renderer.shader

import me.rerere.k3d.util.system.Dirty
import me.rerere.k3d.util.system.dirtyValue

class ShaderProgramSource(
    vertexShader: String,
    fragmentShader: String,
    marcoDefinitions: Set<MarcoDefinition> = emptySet()
) : Dirty {
    private var _dirty = false

    var vertexShader: String by dirtyValue(vertexShader)
    var fragmentShader: String by dirtyValue(fragmentShader)

    private val _marcoDefinitions: MutableSet<MarcoDefinition> = marcoDefinitions.toMutableSet()
    val marcoDefinitions: Set<MarcoDefinition> = _marcoDefinitions

    override fun isDirty(): Boolean {
        return _dirty
    }

    override fun updateDirty() {}

    override fun markDirtyNew() {
        _dirty = true
    }

    override fun clearDirty() {
        _dirty = false
    }

    fun addMarcoDefinition(name: String, value: String? = null) {
        _marcoDefinitions.add(MarcoDefinition(name, value))
        markDirtyNew()
    }

    fun removeMarcoDefinition(name: String) {
        _marcoDefinitions.removeIf {
            it.name == name
        }
        markDirtyNew()
    }

    init {
        require(vertexShader.isNotBlank() && fragmentShader.isNotBlank()) {
            "Shader source cannot be blank"
        }
    }

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
    COLOR("a_color"), // vertex color
    TANGENT("a_tangent"),
    BITANGENT("a_bitangent"),

    JOINTS("a_joints"),
    WEIGHTS("a_weights"),

    TEXCOORD_BASE("a_texCoordBase"),
    TEXCOORD_NORMAL("a_texCoordNormal"),
    TEXCOORD_METALLIC("a_texCoordMetallic"),
    TEXCOORD_ROUGHNESS("a_texCoordRoughness"),
    TEXCOORD_OCCLUSION("a_texCoordOcclusion"),
    TEXCOORD_EMISSIVE("a_texCoordEmissive"),
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

    SKIN_JOINTS_MATRIX("u_skinJointsMatrix"),
    SKIN_JOINTS_MATRIX_SIZE("u_skinJointsMatrixSize"), // the width/height of the data texture
}

enum class BuiltInMarcoDefinition(
    val marcoDefinition: String
) {
    USE_SKIN("USE_SKIN"),
}