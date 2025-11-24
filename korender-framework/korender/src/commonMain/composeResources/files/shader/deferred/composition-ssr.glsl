uniform sampler2D ssrTexture;

#import "!shader/lib/space.glsl"
#import "!shader/lib/pbr.glsl"

#ifdef SSR_ENV
    uniform samplerCube envTexture;
#endif

void compositionSsr() {

    vec4 ssrSample = texture(ssrTexture, vtex);
    color += ssrSample.rgb * ssrSample.a;

#ifdef SSR_ENV
    vec3 vpos = screenToWorldSpace(vtex, depth);
    vec4 albedoTexel = texture(albedoGeometryTexture, vtex);
    vec4 normalTexel = texture(normalGeometryTexture, vtex);
    vec3 albedo = albedoTexel.rgb;
    float metallic = albedoTexel.a;
    float roughness = normalTexel.a;
    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);
    vec3 R = reflect(-V, N);
    vec3 F0 = mix(vec3(0.04), albedo, metallic);
    float maxBias = 8.; // TODO ! Get from da sky
    vec3 envDiffuse = texture(envTexture, N, maxBias).rgb * albedo * (1.0 - metallic);
    vec3 envSpec = texture(envTexture, R, roughness * maxBias).rgb * fresnelSchlick(max(dot(V, N), 0.), F0);

    if (length(normalTexel.rgb) > 0.1)
        color += envDiffuse + envSpec * (1.0 - ssrSample.a);
#endif
}
