package me.rerere.k3d.renderer.shader

class ShaderProcessor {
    private var glslVersion = "300 es"

    fun process(program: ShaderProgramSource): ShaderProgramProcessResult {
        val vertexShader =
            processShader(program.vertexShader, program.marcoDefinitions, ShaderType.VERTEX_SHADER)
        val fragmentShader = processShader(
            program.fragmentShader,
            program.marcoDefinitions,
            ShaderType.FRAGMENT_SHADER
        )
        return ShaderProgramProcessResult(vertexShader, fragmentShader)
    }

    private fun processShader(
        shader: String,
        definition: List<MarcoDefinition>,
        type: ShaderType
    ): String {
        val trimmed = StringBuilder(shader.trim())
        val header = StringBuilder()

        // User should not specify the version in the shader source
        // We will add it automatically
        require(!trimmed.startsWith("#version")) {
            "You should not specify the version in the shader source"
        }

        // Add version
        header.append("#version $glslVersion\n")

        // Add marco definitions
        definition.forEach {
            header.append("#define ${it.name}")
            if (it.value != null) {
                header.append(" ${it.value}")
            }
        }

        return header.append(trimmed).toString()
    }

    enum class ShaderType {
        VERTEX_SHADER,
        FRAGMENT_SHADER
    }
}

data class ShaderProgramProcessResult(
    val vertexShader: String,
    val fragmentShader: String
)