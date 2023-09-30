package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.resource.Uniform
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource
import me.rerere.k3d.util.Color

/**
 * Standard material
 *
 * It is based on Cook-Torrance BRDF, a PBR material, which is more realistic than traditional
 * BRDF models (such as BlinnPhong), but requires more calculations.
 *
 * @see BlinnPhongMaterial
 */
typealias StandardMaterial = CookTorranceMaterial

class CookTorranceMaterial : ShaderMaterial(programSource()) {
    var baseColor by color4fUniformOf(BuiltInUniformName.MATERIAL_COLOR, Color.fromRGBHex("#FFFFFF"))
    var baseColorTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_BASE)

    var normalTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_NORMAL)

    var metallic by floatUniformOf(BuiltInUniformName.MATERIAL_METALLIC, 1.0f)
    var metallicTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_METALLIC)

    var roughness by floatUniformOf(BuiltInUniformName.MATERIAL_ROUGHNESS, 1.0f)
    var roughnessTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_ROUGHNESS)

    var occlusionTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_OCCLUSION)

    var emissive by color3fUniformOf(BuiltInUniformName.MATERIAL_EMISSIVE, Color.fromRGBHex("#000000"))
    var emissiveTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_EMISSIVE)
}

private val programSource: () -> ShaderProgramSource = {
    ShaderProgramSource(
        vertexShader = """ 
    in vec3 a_pos;
    in vec3 a_normal;
    in vec3 a_tangent;
    
    in ivec4 a_joints;
    in vec4 a_weights;
    
    #ifdef SKIN_BONE_COUNT
    uniform mat4 u_skinJointsMatrix[SKIN_BONE_COUNT];
    #endif
    
    out vec3 v_fragPos;
    out vec3 v_normal;
    out mat3 TBN;
    
    uniform mat4 u_modelMatrix;
    uniform mat4 u_viewMatrix;
    uniform mat4 u_projectionMatrix;
    
    in vec2 a_texCoordBase;
    in vec2 a_texCoordNormal;
    in vec2 a_texCoordMetallic;
    in vec2 a_texCoordRoughness;
    in vec2 a_texCoordOcclusion;
    in vec2 a_texCoordEmissive;
    
    out vec2 v_texCoordBase;
    out vec2 v_texCoordNormal;
    out vec2 v_texCoordMetallic;
    out vec2 v_texCoordRoughness;
    out vec2 v_texCoordOcclusion;
    out vec2 v_texCoordEmissive;

    void main() {
        v_texCoordBase = a_texCoordBase;
        v_texCoordNormal = a_texCoordNormal;
        v_texCoordMetallic = a_texCoordMetallic;
        v_texCoordRoughness = a_texCoordRoughness;
        v_texCoordOcclusion = a_texCoordOcclusion;
        v_texCoordEmissive = a_texCoordEmissive;
        
        #ifdef SKIN_BONE_COUNT
            mat4 skinJointsMatrix = u_skinJointsMatrix[a_joints[0]] * a_weights[0];
            skinJointsMatrix += u_skinJointsMatrix[a_joints[1]] * a_weights[1];
            skinJointsMatrix += u_skinJointsMatrix[a_joints[2]] * a_weights[2];
            skinJointsMatrix += u_skinJointsMatrix[a_joints[3]] * a_weights[3];
            mat4 modelMatrix = skinJointsMatrix;
        #else  
            mat4 modelMatrix = u_modelMatrix;
        #endif

        gl_Position = u_projectionMatrix * u_viewMatrix * modelMatrix * vec4(a_pos, 1.0);
        
        v_normal = mat3(transpose(inverse(modelMatrix))) * a_normal;
        v_fragPos = vec3(modelMatrix * vec4(a_pos, 1.0));

        // calculate TBN matrix for normal mapping
        // assume the scale is equal in all directions
        vec3 T = normalize(mat3(modelMatrix) * a_tangent);
        vec3 N = normalize(mat3(modelMatrix) * a_normal);
        T = normalize(T - dot(T, N) * N); // Gram-Schmidt
        vec3 B = cross(N, T);
        TBN = mat3(T, B, N);
    }
""".trimIndent(),
        fragmentShader = """
    #include "light"
    #include "cook_torrance_brdf"
    
    out vec4 fragColor;
    
    in vec3 v_normal;
    in vec3 v_fragPos;
    in mat3 TBN;
    
    in vec2 v_texCoordBase;
    in vec2 v_texCoordNormal;
    in vec2 v_texCoordMetallic;
    in vec2 v_texCoordRoughness;
    in vec2 v_texCoordOcclusion;
    in vec2 v_texCoordEmissive;

    uniform sampler2D u_textureBase;
    uniform sampler2D u_textureNormal;
    uniform sampler2D u_textureMetallic;
    uniform sampler2D u_textureRoughness;
    uniform sampler2D u_textureOcclusion;
    uniform sampler2D u_textureEmissive;
    uniform vec3 u_cameraPos;
    
    uniform vec4 u_materialColor;
    uniform float u_materialRoughness;
    uniform float u_materialMetallic;
    uniform vec3 u_materialEmissive;
    
    vec3 getNormal() {     
        vec3 normalFromMap = texture(u_textureNormal, v_texCoordNormal).rgb;
        normalFromMap = normalize(normalFromMap * 2.0 - 1.0);
        vec3 normal = normalize(TBN * normalFromMap);

        return normal;
    }
    
    vec3 toLinear(vec3 sRGB) {
        bvec3 cutoff = lessThan(sRGB, vec3(0.04045));
        vec3 higher = pow((sRGB + vec3(0.055))/vec3(1.055), vec3(2.4));
        vec3 lower = sRGB/vec3(12.92);

        return mix(higher, lower, cutoff);
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
        
        float NdotL = max(dot(N, L), 0.0);
        return (diffuse + specular) * NdotL * lightColor * lightIntensity;
    }
    
    void main() {
        fragColor = vec4(1.0);
        //return;
        vec3 albedo = u_materialColor.rgb;
        float opacity = u_materialColor.a;
        
        // texture base
        #ifdef HAS_TEXTURE_u_textureBase
            albedo *= toLinear(texture(u_textureBase, v_texCoordBase).rgb);
            opacity *= texture(u_textureBase, v_texCoordBase).a;
        #endif 

        // normal
        #ifdef HAS_TEXTURE_u_textureNormal
            vec3 normal = getNormal();
        #else
            vec3 normal = normalize(v_normal);
        #endif
        
        // roughness & metallic
        float roughness = u_materialRoughness;
        #ifdef HAS_TEXTURE_u_textureRoughness
            roughness *= texture(u_textureRoughness, v_texCoordRoughness).g;
        #endif
        float metallic = u_materialMetallic;
        #ifdef HAS_TEXTURE_u_textureMetallic
            metallic *= texture(u_textureMetallic, v_texCoordMetallic).b;
        #endif
       
        // ambient
        vec3 ambient = ambientLight.color * ambientLight.intensity * albedo;
        #ifdef HAS_TEXTURE_u_textureOcclusion
            ambient *= texture(u_textureOcclusion, v_texCoordOcclusion).r;
        #endif
        
        vec3 lighting = vec3(0.0);
        
        // directional light
        {
            vec3 lightDir = -normalize(directionalLight.target - directionalLight.position);
            vec3 viewDir = -normalize(v_fragPos - u_cameraPos);
            vec3 cookTorrance = cookTorranceBRDF(lightDir, viewDir, normal, albedo, metallic, roughness, directionalLight.color, directionalLight.intensity);
            lighting += max(cookTorrance, vec3(0.0));
        }
        
        // point light
        {
            for(int i = 0; i < pointLightCount; i++) {
                vec3 lightDir = -normalize(v_fragPos - pointLight[i].position);
                vec3 viewDir = -normalize(v_fragPos - u_cameraPos);
                
                float distance = length(v_fragPos - pointLight[i].position);
                float attenuation = 1.0 / (pointLight[i].constant + pointLight[i].linear * distance + pointLight[i].quadratic * distance * distance);
                float intensity = pointLight[i].intensity * attenuation;
                
                vec3 cookTorrance = cookTorranceBRDF(lightDir, viewDir, normal, albedo, metallic, roughness, pointLight[i].color, intensity);
                lighting += max(cookTorrance, vec3(0.0));
            }
        }
        
        // spot light
        {
            for(int i = 0; i < spotLightCount; i++) {
                vec3 lightDir = -normalize(v_fragPos - spotLight[i].position);
                vec3 viewDir = -normalize(v_fragPos - u_cameraPos);
                
                float penumbra = spotLight[i].penumbra; // spot light penumbra (0.0 ~ 1.0)
                float angle = spotLight[i].angle; // spot light angle in radians
                
                float angleMin = cos(angle);
                float angleMax = cos(angle * (1.0 - penumbra));
                float cosAngle = dot(lightDir, normalize(spotLight[i].position - spotLight[i].target));
                float spotEffect = smoothstep(angleMin, angleMax, cosAngle);
                
                float intensity = spotLight[i].intensity * spotEffect;
                
                vec3 cookTorrance = cookTorranceBRDF(lightDir, viewDir, normal, albedo, metallic, roughness, spotLight[i].color, intensity);
                lighting += max(cookTorrance, vec3(0.0));
            }
        }
        
        // combine
        vec3 result = ambient + lighting;
        
        // emissive
        #ifdef HAS_TEXTURE_u_textureEmissive
            result += toLinear(texture(u_textureEmissive, v_texCoordEmissive).rgb) * u_materialEmissive;
        #else
            result += u_materialEmissive;
        #endif
        
        fragColor = vec4(result, opacity);
        
        // HDR tone mapping
        fragColor.rgb = fragColor.rgb / (fragColor.rgb + vec3(1.0));
        
        // Gamma correction (2.2)
        fragColor.rgb = pow(fragColor.rgb, vec3(1.0/2.2));
    }
""".trimIndent()
    )
}