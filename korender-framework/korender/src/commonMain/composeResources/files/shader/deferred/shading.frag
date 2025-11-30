#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D albedoGeometryTexture;
uniform sampler2D normalGeometryTexture;
uniform sampler2D emissionGeometryTexture;
uniform sampler2D depthGeometryTexture;

uniform sampler2D shadowTextures[5];
uniform sampler2DShadow pcfTextures[5];

#uniforms

//////////

out vec4 fragColor;

float shadowRatios[5];

#import "!shader/lib/space.glsl"

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef PLUGIN_SKY
    #import "!shader/lib/sky.glsl"
    #import "$sky"
    #import "!shader/lib/skyibl.glsl"
#endif

void main() {

    float depth = texture(depthGeometryTexture, vtex).r;

    vec3 vpos = screenToWorldSpace(vtex, depth);

    vec4 albedoTexel = texture(albedoGeometryTexture, vtex);
    vec4 normalTexel = texture(normalGeometryTexture, vtex);
    vec4 emissionTexel = texture(emissionGeometryTexture, vtex);

    vec3 albedo = albedoTexel.rgb;
    float metallic = albedoTexel.a;
    float roughness = normalTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    vec3 color = ambientColor * albedo.rgb * (1.0 - metallic) + emissionTexel.rgb;

    float plane = dot((vpos - cameraPos), cameraDir);
    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++)
        color += dirLight(l, N, V, albedo, metallic, roughness, 1.0);

    for (int l=0; l<numPointLights; l++)
        color += pointLight(vpos, l, N, V, albedo, metallic, roughness, 1.0);

    #ifdef PLUGIN_SKY
        float roughnessAA = antiAliasRoughness(roughness, N, V);
        color += skyibl(N, V, albedo, metallic, roughnessAA);
    #endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}