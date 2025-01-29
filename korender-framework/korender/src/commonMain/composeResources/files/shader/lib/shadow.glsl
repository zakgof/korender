float vsm(sampler2D shadowTexture, vec3 vshadow) {
    const float bias = 0.005;

    #ifdef WEBGL
    const float varianceMagic = 0.001;
    #else
    const float varianceMagic = 0.00001;
    #endif

    vec3 moments = texture(shadowTexture, vshadow.xy).rgb;
    float p = vshadow.z * vshadow.z - moments.x;

    float hardStep = smoothstep(0., bias, p);
    float variance = moments.y - moments.x * moments.x;

    variance = max(variance, varianceMagic);
    float hardness =  p * p / (variance + p * p);

    if (vshadow.x < 0.001 || vshadow.x > 0.999 || vshadow.y < 0.001 || vshadow.y > 0.999)
        return 0.;

    return hardStep * hardness;
}

vec2 vogelDiskSample(int sampleIndex, int numSamples, float phi) {
    float goldenAngle = 2.39996323;
    float sampleVal = float(sampleIndex);
    float angle = sampleVal * goldenAngle + phi;
    return vec2(cos(angle), sin(angle)) * sqrt(sampleVal + 0.5) / sqrt(float(numSamples));
}

float pcss(sampler2D shadowTexture, vec3 vshadow, int sampleCount, float penumbraWidth) {
    float beavis = 0.002;

    const float PHI = 1.61803398874989484820459;
    float phi = 0.; // 6.28 * fract(tan(distance(vpos.xy * 20.0 * PHI, vpos.xy * 20.0) * 0.01) * vpos.x);

    float cumulative = 0.;
    float weight = 0.;
    vec2 dx = dFdx(vshadow.xy);
    vec2 dy = dFdy(vshadow.xy);
    for (int s = 0; s < sampleCount; ++s) {
        vec2 offset = vogelDiskSample(s, sampleCount, phi) * penumbraWidth;
        vec2 uv = vshadow.xy + offset;
        float shadowSample = textureGrad(shadowTexture, uv, dx, dy).r;
        float val = (shadowSample < vshadow.z - beavis
            && uv.x > 0.001 && uv.x < 0.999
            && uv.y > 0.001 && uv.y < 0.999) ? 1. : 0.;
        cumulative += val;
        weight += 1.;
    }
    return cumulative / weight;
}

float hard(sampler2D shadowTexture, vec3 vshadow) {
    float beavis = 0.002;
    float shadowSample = texture(shadowTexture, vshadow.xy).r;

    return (shadowSample < vshadow.z - beavis
        && vshadow.x > 0.001 && vshadow.x < 0.999
        && vshadow.y > 0.001 && vshadow.y < 0.999) ? 1. : 0.;
}

float shadow(sampler2D shadowTexture, int index, vec3 vshadow, int mode) {
    float sh = 0.;
    switch (mode) {
          case 0: sh = hard(shadowTexture, vshadow); break;
          case 1: sh =  pcss(shadowTexture, vshadow, i1[index], f1[index]); break;
          case 2: sh =  vsm(shadowTexture, vshadow); break;
    }
    return sh;
}

float casc(int s, float plane, sampler2D shadowTexture) {
    vec3 vshadow = (bsps[s] * vec4(vpos, 1.0)).xyz;
    if ((shadowMode[s] & 0x80) != 0) {
        vshadow.z = (yMax[s] - vpos.y) / (yMax[s] - yMin[s]);
    }
    float sh = shadow(shadowTexture, s, vshadow, shadowMode[s] & 0x07);
    vec4 ci = cascade[s];
    float cascadeContribution = smoothstep(ci.r, ci.g, plane) * (1.0 - smoothstep(ci.b, ci.a, plane));
    return sh * cascadeContribution;
}

vec3 dirLight(int l, vec3 N, vec3 V, vec3 c_diff, vec3 F0, float rough) {
    float shadowRatio = 0.;
    int shadowCount = directionalLightShadowTextureCount[l];
    for (int c=0; c<shadowCount; c++) {
        int idx = directionalLightShadowTextureIndex[l] + c;
        shadowRatio += shadowRatios[idx];
    }
    vec3 lightValue = directionalLightColor[l].rgb * (1. - shadowRatio);
    vec3 L = normalize(-directionalLightDir[l]);
    return calculatePBR(N, V, L, c_diff, F0, rough, lightValue);
}