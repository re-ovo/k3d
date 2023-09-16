package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.shader.ShaderProgram

private val StandProgram = ShaderProgram(
    vertexShader = """
        #version 300 es
        
        in vec3 a_pos;
        in vec3 a_normal;
        
        out vec3 v_normal;
        
        uniform mat4 u_worldMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_worldMatrix * vec4(a_pos, 1.0);
            v_normal = transpose(inverse(mat3(u_worldMatrix))) * a_normal;
        }
    """.trimIndent(),
    fragmentShader = """
        #version 300 es
        precision mediump float;
        
        in vec3 v_normal;
        out vec4 fragColor;
        
        void main() {
            vec3 normal = normalize(v_normal);
            vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
            fragColor = vec4(vec3(0.5) + vec3(0.5) * dot(normal, lightDir), 1.0);
        }
    """.trimIndent()
)

class StandardMaterial : ShaderMaterial(StandProgram, emptySet()) {
}