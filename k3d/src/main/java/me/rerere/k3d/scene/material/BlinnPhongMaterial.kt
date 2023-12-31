package me.rerere.k3d.scene.material

import me.rerere.k3d.renderer.resource.Texture
import me.rerere.k3d.renderer.shader.BuiltInUniformName
import me.rerere.k3d.renderer.shader.ShaderProgramSource

class BlinnPhongMaterial : ShaderMaterial(programSource()) {
    var baseColorTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_BASE)

    var normalTexture: Texture? by textureOf(BuiltInUniformName.TEXTURE_NORMAL)
}

private val programSource : () -> ShaderProgramSource = {
    ShaderProgramSource(
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
    
    in vec3 v_normal;
    in vec3 v_fragPos;
    in mat3 TBN;
    in vec2 v_texCoordBase;
    in vec2 v_texCoordNormal;
  
    out vec4 fragColor;
    
    uniform sampler2D u_textureBase;
    uniform sampler2D u_textureNormal;
    uniform vec3 u_cameraPos;
    
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
    
    void main() {
        #ifdef HAS_TEXTURE_u_textureBase
            vec3 albedo = texture(u_textureBase, v_texCoordBase).rgb;
            albedo = toLinear(vec4(albedo, 1.0)).rgb;
            vec3 lightDir = normalize(directionalLight.target - directionalLight.position);
            
            // ambient
            vec3 ambient = ambientLight.color * ambientLight.intensity * albedo;
            
            // diffuse
            vec3 normal = normalize(v_normal);
            float diff = max(dot(normal, -lightDir), 0.0);
            vec3 diffuse = directionalLight.color * directionalLight.intensity * diff;
            
            // specular
            vec3 viewDir = normalize(v_fragPos - u_cameraPos);
            vec3 reflectDir = reflect(lightDir, normal);
            float spec = pow(max(dot(-viewDir, reflectDir), 0.0), 32.0);
            vec3 specular = directionalLight.color * directionalLight.intensity * spec;
            
            // combine results
            vec3 result = (ambient + diffuse + specular) * albedo;
            fragColor = vec4(result, 1.0);
        #else
            vec3 albedo = vec3(1.0, 0.0, 0.0);
            vec3 lightDir = normalize(vec3(1.0, 1.0, 1.0));
            float diff = max(dot(v_normal, lightDir), 0.1);
            fragColor = vec4(albedo * diff, 1.0);
        #endif
        
        // HDR tone mapping
        fragColor.rgb = fragColor.rgb / (fragColor.rgb + vec3(1.0));
        
        // Gamma correction (2.2)
        fragColor.rgb = pow(fragColor.rgb, vec3(1.0/2.2));
    }
""".trimIndent()
    )
}