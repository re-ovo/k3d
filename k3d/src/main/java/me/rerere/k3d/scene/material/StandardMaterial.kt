package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgram

private val StandProgram = ShaderProgram(
    vertexShader = """
        #version 300 es
        
        in vec3 a_pos;
        in vec3 a_normal;
        in vec2 a_texCoordBase;
        
        out vec3 v_normal;
        out vec2 v_texCoordBase;
        
        uniform mat4 u_worldMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_worldMatrix * vec4(a_pos, 1.0);
            
            v_normal = mat3(transpose(inverse(u_worldMatrix))) * a_normal;
            v_texCoordBase = a_texCoordBase;
        }
    """.trimIndent(),
    fragmentShader = """
        #version 300 es
        precision mediump float;
        
        in vec3 v_normal;
        in vec2 v_texCoordBase;
      
        out vec4 fragColor;
        
        uniform sampler2D u_textureBase;
        
        #define USE_TEXTURE_BASE
        
        void main() {
            vec3 normal = normalize(v_normal);
            vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
            float diff = max(dot(normal, lightDir), 0.0) + 0.2;
            
            vec4 baseColor = texture(u_textureBase, v_texCoordBase);
            
            #ifdef USE_TEXTURE_BASE
                fragColor = vec4(baseColor.rgb * diff, baseColor.a);
            #else
                fragColor = vec4(diff, diff, diff, 1.0);
            #endif
        }
    """.trimIndent()
)

class StandardMaterial : ShaderMaterial(StandProgram) {
    var baseColorTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_BASE.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_BASE.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_BASE.uniformName] = value
            }
        }
}