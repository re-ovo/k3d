package me.rerere.k3d.renderer.shader

// TODO: 添加一个缓存或者校验防止开发者在使用时重复创建相同的ShaderProgram
data class ShaderProgram(
    val vertexShader: String,
    val fragmentShader: String
) {
    init {
        require(vertexShader.isNotBlank() && fragmentShader.isNotBlank()) {
            "Shader source cannot be blank"
        }
        println(vertexShader.trimIndent())
        println(fragmentShader.trimIndent())
    }
}