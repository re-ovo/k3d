package me.rerere.k3d.renderer.shader.include

internal val LightGLSL = """
struct AmbientLight {
    vec3 position;
    vec3 color;
    float intensity;
};

struct DirectionalLight {
    vec3 position;
    vec3 target;
    vec3 color;
    float intensity;
};

struct PointLight {
    vec3 position;
    vec3 color;
    float intensity;
    float distance;
    float decay;
};

struct SpotLight {
    vec3 position;
    vec3 target;
    vec3 color;
    float intensity;
    float distance;
    float decay;
    float angle;
    float penumbra;
};

uniform AmbientLight ambientLight;
uniform DirectionalLight directionalLight;

uniform PointLight pointLight[4];
uniform int pointLightCount;

uniform SpotLight spotLight[4];
uniform int spotLightCount;
""".trimIndent()