uniform sampler2D ssrTexture;

#import "!shader/lib/space.glsl"

#ifdef SSR_ENV
    uniform samplerCube envTexture;
#endif

void compositionSsr() {

    vec4 ssrSample = texture(ssrTexture, vtex);
    color += ssrSample.rgb * ssrSample.a;

#ifdef SSR_ENV
    vec3 vpos = screenToWorldSpace(vtex, depth);
    vec4 materialTexel = texture(materialGeometryTexture, vtex);
    vec4 normalTexel = texture(normalGeometryTexture, vtex);
    vec3 F0 = materialTexel.rgb;
    float roughness = materialTexel.a;
    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    vec3 R = reflect(-V, N);
    float NdotV = max(dot(N, V), 0.0);
    float maxBias = 8.; // TODO ! Get from da sky
    vec3 envColor = texture(envTexture, R, roughness * maxBias).rgb;
    vec3 FR = F0 + (1. - F0) * pow(1. - NdotV, 5.);
    color += envColor * FR * (1. - ssrSample.a);
#endif

}
