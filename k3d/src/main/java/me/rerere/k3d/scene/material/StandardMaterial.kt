package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.shader.ShaderProgram

private val StandProgram = ShaderProgram(
    vertexShader = """
        #version 300 es
        
        in vec3 a_pos;
        
        uniform mat4 u_worldMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_worldMatrix * vec4(a_pos, 1.0);
        }
    """.trimIndent(),
    fragmentShader = """
        #version 300 es
        precision mediump float;
        
        out vec4 fragColor;
        
        void main() {
            fragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
    """.trimIndent()
)

class StandardMaterial : ShaderMaterial(StandProgram, emptySet()) {
}