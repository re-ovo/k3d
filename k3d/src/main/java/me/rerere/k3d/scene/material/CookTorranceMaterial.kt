package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource

class CookTorranceMaterial : ShaderMaterial(programSource) {
    var roughness by uniformOf(BuiltInUniformName.MATERIAL_METALLIC, Uniform.Float1(1.0f))

    var metallic by uniformOf(BuiltInUniformName.MATERIAL_ROUGHNESS, Uniform.Float1(1.0f))

    var baseColorTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_BASE)

    var normalTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_NORMAL)

    var metallicTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_METALLIC)

    var roughnessTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_ROUGHNESS)

    var occlusionTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_OCCLUSION)
}

private val programSource = ShaderProgramSource(
    vertexShader = """ 
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
        
        uniform mat4 u_modelMatrix;
        uniform mat4 u_viewMatrix;
        uniform mat4 u_projectionMatrix;
        
        void main() {
            gl_Position = u_projectionMatrix * u_viewMatrix * u_modelMatrix * vec4(a_pos, 1.0);
            
            v_normal = mat3(transpose(inverse(u_modelMatrix))) * a_normal;
            v_fragPos = vec3(u_modelMatrix * vec4(a_pos, 1.0));
            
            v_texCoordBase = a_texCoordBase;
            v_texCoordNormal = a_texCoordNormal;
            
            // calculate TBN matrix for normal mapping
            // assume the scale is equal in all directions
            vec3 T = normalize(mat3(u_modelMatrix) * a_tangent);
            vec3 N = normalize(mat3(u_modelMatrix) * a_normal);
            T = normalize(T - dot(T, N) * N); // Gram-Schmidt
            vec3 B = cross(N, T);
            TBN = mat3(T, B, N);
        }
    """.trimIndent(),
    fragmentShader = """
        #include "light"
        #include "cook_torrance_brdf"
        
        in vec3 v_normal;
        in vec3 v_fragPos;
        in mat3 TBN;
        in vec2 v_texCoordBase;
        in vec2 v_texCoordNormal;
      
        out vec4 fragColor;
        
        uniform sampler2D u_textureBase;
        uniform sampler2D u_textureNormal;
        uniform sampler2D u_textureMetallic;
        uniform sampler2D u_textureRoughness;
        uniform sampler2D u_textureOcclusion;
        uniform vec3 u_cameraPos;
        
        uniform float u_materialRoughness;
        uniform float u_materialMetallic;
        
        vec3 getNormal() {     
            vec3 normalFromMap = texture(u_textureNormal, v_texCoordNormal).rgb;
            normalFromMap = normalize(normalFromMap * 2.0 - 1.0);
            vec3 normal = normalize(TBN * normalFromMap);
    
            return normal;
        }
        
        vec4 toLinear(vec4 sRGB) {
            bvec3 cutoff = lessThan(sRGB.rgb, vec3(0.04045));
            vec3 higher = pow((sRGB.rgb + vec3(0.055))/vec3(1.055), vec3(2.4));
            vec3 lower = sRGB.rgb/vec3(12.92);

            return vec4(mix(higher, lower, cutoff), sRGB.a);
        }
        
        // Cook-Torrance BRDF
        // L: light direction
        // V: view direction
        // N: normal
        vec3 cookTorranceBRDF(vec3 L, vec3 V, vec3 N, vec3 albedo, float metallic, float roughness, vec3 lightColor, float lightIntensity) {
            vec3 H = normalize(V + L); // half vector
            vec3 F0 = vec3(0.04);
            F0 = mix(F0, albedo, metallic);

            // Fresnel
            float cosTheta = dot(H, V);
            vec3 F = fresnelSchlick(cosTheta, F0);

            // Distribution
            float D = distributionGGX(N, H, roughness);

            // Geometry
            float G = geometrySmith(N, V, L, roughness);

            // Specular BRDF
            vec3 numerator = D * G * F;
            float denominator = 4.0 * max(dot(N, V), 0.0) * max(dot(N, L), 0.0) + 0.001;
            vec3 specular = numerator / denominator;

            // Diffuse BRDF
            vec3 kS = F;
            vec3 kD = vec3(1.0) - kS;
            kD *= 1.0 - metallic;	  
            vec3 diffuse = kD * albedo / 3.14159265358979323846;

            return (diffuse + specular) * max(dot(N, L), 0.0) * lightColor * lightIntensity;
        }
        
        void main() {
            #ifdef HAS_TEXTURE_u_textureBase
                vec3 albedo = texture(u_textureBase, v_texCoordBase).rgb;
                albedo = toLinear(vec4(albedo, 1.0)).rgb;
            #else
                vec3 albedo = vec3(1.0, 0.0, 0.0);
            #endif
            
            vec3 lightDir = -normalize(directionalLight.target - directionalLight.position);
                
            // ambient
            vec3 ambient = ambientLight.color * ambientLight.intensity * albedo;
            
            // cook-torrance
            vec3 normal = getNormal();
            vec3 viewDir = -normalize(v_fragPos - u_cameraPos);
            float roughness = texture(u_textureRoughness, v_texCoordBase).g;
            float metallic = texture(u_textureMetallic, v_texCoordBase).b;
            vec3 cookTorrance = cookTorranceBRDF(lightDir, viewDir, normal, albedo, metallic, roughness, directionalLight.color, directionalLight.intensity);
            
            // combine results
            vec3 result = (ambient + cookTorrance);
            fragColor = vec4(result, 1.0);
            
            // HDR tone mapping
            fragColor.rgb = fragColor.rgb / (fragColor.rgb + vec3(1.0));
            
            // Gamma correction (2.2)
            fragColor.rgb = pow(fragColor.rgb, vec3(1.0/2.2));
        }
    """.trimIndent()
)