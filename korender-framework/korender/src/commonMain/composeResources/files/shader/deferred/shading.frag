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

float shadowRatios[5] = float[5](0., 0., 0., 0., 0.);

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

    vec3 color = emissionTexel.rgb;
    float occlusion = emissionTexel.a;

    float plane = dot((vpos - cameraPos), cameraDir);
    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++)
        color += dirLight(l, N, V, albedo, metallic, roughness, occlusion);

    for (int l=0; l<numPointLights; l++)
        color += pointLight(vpos, l, N, V, albedo, metallic, roughness, occlusion);

    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    vec3 diffFactor = albedo * (1.0 - metallic);
    vec3 specFactor = fresnelSchlick(max(dot(V, N), 0.1), F0);
    color += ambientColor * (diffFactor + specFactor * 0.3);
    #ifdef PLUGIN_SKY
        color += skyibl(N, V, roughness, diffFactor, specFactor);
    #endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}