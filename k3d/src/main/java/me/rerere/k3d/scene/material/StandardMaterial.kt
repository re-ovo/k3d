package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgram

private val StandProgram = ShaderProgram(
    vertexShader = """
        #version 300 es
        
        in vec3 a_pos;
        in vec3 a_normal;
        in vec3 a_tangent;
        in vec2 a_texCoordBase;
        in vec2 a_texCoordNormal;
        
        out vec3 v_fragPos;
        out vec3 v_normal;
        out mat3 TBN;
        
        out vec2 v_texCoordBase;
        out vec2 v_texCoordNormal;
        
        uniform mat4 u_worldMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_worldMatrix * vec4(a_pos, 1.0);
            
            v_normal = mat3(transpose(inverse(u_worldMatrix))) * a_normal;
            v_fragPos = vec3(u_worldMatrix * vec4(a_pos, 1.0));
            
            v_texCoordBase = a_texCoordBase;
            v_texCoordNormal = a_texCoordNormal;
            
            // calculate TBN matrix for normal mapping
            // assume the scale is equal in all directions
            vec3 T = normalize(mat3(u_worldMatrix) * a_tangent);
            vec3 N = normalize(mat3(u_worldMatrix) * a_normal);
            T = normalize(T - dot(T, N) * N); // Gram-Schmidt
            vec3 B = cross(N, T);
            TBN = mat3(T, B, N);
        }
    """.trimIndent(),
    fragmentShader = """
        #version 300 es
        precision mediump float;
        
        in vec3 v_normal;
        in vec3 v_fragPos;
        in mat3 TBN;
        in vec2 v_texCoordBase;
        in vec2 v_texCoordNormal;
      
        out vec4 fragColor;
        
        uniform sampler2D u_textureBase;
        uniform sampler2D u_textureNormal;
        
        #define USE_TEXTURE_BASE
        
        vec3 getNormal() {     
            vec3 normalFromMap = texture(u_textureNormal, v_texCoordNormal).rgb;
            normalFromMap = normalize(normalFromMap * 2.0 - 1.0);
            vec3 normal = normalize(TBN * normalFromMap);
    
            return normal;
        }
        
        void main() {
            vec3 normal = getNormal();
            vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
            float diff = max(dot(normal, lightDir), 0.0);
            
            vec4 baseColor = texture(u_textureBase, v_texCoordBase);
            
            #ifdef USE_TEXTURE_BASE
                fragColor = vec4(baseColor.rgb * diff, baseColor.a);
            #else
                fragColor = vec4(normal * 0.5 + 0.5, 1.0);
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

    var normalTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_NORMAL.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_NORMAL.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_NORMAL.uniformName] = value
            }
        }

    var metallicTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_METALLIC.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_METALLIC.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_METALLIC.uniformName] = value
            }
        }

    var roughnessTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_ROUGHNESS.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_ROUGHNESS.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_ROUGHNESS.uniformName] = value
            }
        }

    var occlusionTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_OCCLUSION.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_OCCLUSION.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_OCCLUSION.uniformName] = value
            }
        }

    var emissiveTexture: Texture?
        get() = textures[BuiltInUniformName.TEXTURE_EMISSIVE.uniformName]
        set(value) {
            if (value == null) {
                textures.remove(BuiltInUniformName.TEXTURE_EMISSIVE.uniformName)
            } else {
                textures[BuiltInUniformName.TEXTURE_EMISSIVE.uniformName] = value
            }
        }
}