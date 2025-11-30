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

float swPcf(sampler2D shadowTexture, vec3 vshadow, int sampleCount, float penumbraWidth, float bias) {

    const float PHI = 1.61803398874989484820459;
    float phi = 0.;

    float cumulative = 0.;
    float weight = 0.;
    vec2 dx = dFdx(vshadow.xy);
    vec2 dy = dFdy(vshadow.xy);
    for (int s = 0; s < sampleCount; ++s) {
        vec2 offset = vogelDiskSample(s, sampleCount, phi) * penumbraWidth;
        vec2 uv = vshadow.xy + offset;
        float shadowSample = textureGrad(shadowTexture, uv, dx, dy).r;
        float val = (shadowSample < vshadow.z - bias
            && uv.x > 0.001 && uv.x < 0.999
            && uv.y > 0.001 && uv.y < 0.999) ? 1. : 0.;
        cumulative += val;
        weight += 1.;
    }
    return cumulative / weight;
}

float hard(sampler2D shadowTexture, vec3 vshadow) {
    float beavis = 0.005;
    float shadowSample = texture(shadowTexture, vshadow.xy).r;

    return (shadowSample < vshadow.z - beavis
        && vshadow.x > 0.001 && vshadow.x < 0.999
        && vshadow.y > 0.001 && vshadow.y < 0.999) ? 1. : 0.;
}

float hwPcf(sampler2DShadow pcfTexture, vec3 vshadow, float bias) {
    return 1. - texture(pcfTexture, vshadow - vec3(0., 0., bias));
}

float shadow(sampler2D shadowTexture, sampler2DShadow pcfTexture, int index, vec3 vshadow, int mode) {
    float sh = 0.;
    switch (mode) {
          case 0: sh = hard(shadowTexture, vshadow); break;
          case 1: sh =  swPcf(shadowTexture, vshadow, i1[index], f1[index], f2[index]); break;
          case 2: sh =  vsm(shadowTexture, vshadow); break;
          case 3: sh =  hwPcf(pcfTexture, vshadow, f1[index]); break;
    }
    return sh;
}

float linstep(float edge0, float edge1, float x) {
    return clamp((x - edge0) / (edge1 - edge0), 0.0, 1.0);
}

float casc(int s, float plane, vec3 vpos, sampler2D shadowTexture, sampler2DShadow pcfTexture) {
    vec3 vshadow = (bsps[s] * vec4(vpos, 1.0)).xyz;
    if ((shadowMode[s] & 0x80) != 0) {
        vshadow.z = (yMax[s] - vpos.y) / (yMax[s] - yMin[s]);
    }
    float sh = shadow(shadowTexture, pcfTexture, s, vshadow, shadowMode[s] & 0x07);
    vec4 ci = cascade[s];
    float cascadeContribution = linstep(ci.r, ci.g, plane) * (1.0 - linstep(ci.b, ci.a, plane));
    return sh * cascadeContribution;
}

void populateShadowRatios(float plane, vec3 vpos) {
    if (numShadows > 0) shadowRatios[0] = casc(0, plane, vpos, shadowTextures[0], pcfTextures[0]);
    if (numShadows > 1) shadowRatios[1] = casc(1, plane, vpos, shadowTextures[1], pcfTextures[1]);
    if (numShadows > 2) shadowRatios[2] = casc(2, plane, vpos, shadowTextures[2], pcfTextures[2]);
    if (numShadows > 3) shadowRatios[3] = casc(3, plane, vpos, shadowTextures[3], pcfTextures[3]);
    if (numShadows > 4) shadowRatios[4] = casc(4, plane, vpos, shadowTextures[4], pcfTextures[4]);
}