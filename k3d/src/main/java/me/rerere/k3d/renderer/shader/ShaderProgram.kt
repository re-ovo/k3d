package me.rerere.k3d.renderer.shader

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