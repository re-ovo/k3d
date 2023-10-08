package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.shader.ShaderProgramSource

class LineBasicMaterial : ShaderMaterial(
    ShaderProgramSource(
        vertexShader = """
            in vec3 a_pos;
            
            uniform mat4 u_modelMatrix;
            uniform mat4 u_viewMatrix;
            uniform mat4 u_projectionMatrix;
            
            void main() {
                gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * vec4(a_pos, 1.0);
            }
        """.trimIndent(),
        fragmentShader = """
            out vec4 fragColor;
            void main() {
                fragColor = vec4(1.0, 0.0, 0.0, 1.0);
            }
        """.trimIndent()
    )
)

