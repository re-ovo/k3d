package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource

class PointMaterial : ShaderMaterial(
    ShaderProgramSource(
        vertexShader = """
        in vec3 a_pos;
        
        uniform mat4 u_modelMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        uniform float u_pointSize;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * vec4(a_pos, 1.0);
            gl_PointSize = u_pointSize;
        }
    """.trimIndent(),
        fragmentShader = """
        out vec4 fragColor;
        
        void main() {
            fragColor = vec4(1.0, 0.0, 0.0, 1.0);
        }
    """.trimIndent()
    )
) {
    var pointSize by floatUniformOf(BuiltInUniformName.POINT_SIZE, 10f)
}