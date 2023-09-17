package me.rerere.k3d.renderer.shader

// TODO: 添加一个缓存或者校验防止开发者在使用时重复创建相同的ShaderProgram
data class ShaderProgram(
    var vertexShader: String,
    var fragmentShader: String
) {
    init {
        require(vertexShader.isNotBlank() && fragmentShader.isNotBlank()) {
            "Shader source cannot be blank"
        }
        println(vertexShader.trimIndent())
        println(fragmentShader.trimIndent())
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
    TEXCOORD_EMISIVE("a_texCoordEmisive"),
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
    TEXTURE_EMISIVE("u_textureEmisive"),

    CAMERA_POSITION("u_cameraPosition"),
}