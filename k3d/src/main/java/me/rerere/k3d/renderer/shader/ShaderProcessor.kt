package me.rerere.k3d.renderer.shader

import me.rerere.k3d.renderer.shader.include.LightGLSL

class ShaderProcessor {
    private var glslVersion = "300 es"
    private val includes = mutableMapOf(
        "light" to LightGLSL,
    )

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
        val lines = trimmed.split("\n")
        val header = StringBuilder()

        // User should not specify the version in the shader source
        // We will add it automatically
        require(!trimmed.startsWith("#version")) {
            "You should not specify the version in the shader source"
        }

        // Add version
        header.append("#version $glslVersion\n")

        // Add precision
        if (type == ShaderType.FRAGMENT_SHADER && lines.none { it.startsWith("precision ") }) {
            header.append("precision mediump float;\n")
        }

        // Add includes
        lines.forEach {
            if (it.startsWith("#include")) {
                val includeName = it.split(" ")[1].removeSurrounding("\"")
                val include = includes[includeName]
                require(include != null) {
                    "Include $includeName not found"
                }
                header.append(include)
                header.append("\n")

                // Remove the include line
                trimmed.deleteRange(
                    trimmed.indexOf(it),
                    trimmed.indexOf(it) + it.length
                )
            }
        }

        // Add marco definitions
        definition.forEach {
            header.append("#define ${it.name}")
            if (it.value != null) {
                header.append(" ${it.value}")
            }
            header.append("\n")
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