float vsm(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {
    vec2 moments = texture(shadowTexture, vshadow.xy).rg;

    if (vshadow.x < 0.001 || vshadow.x > 0.999 || vshadow.y < 0.001 || vshadow.y > 0.999 || vshadow.z > 0.999)
        return 0.;

    float p = vshadow.z - moments.x;

    // TODO adjust variance clamp depending on platform
    float variance = max(moments.y - moments.x * moments.x, 0.0002);
    return 1.0 - variance / (variance + p * p);
}

vec2 vogelDiskSample(int sampleIndex, int numSamples, float phi) {
    float goldenAngle = 2.39996323;
    float sampleVal = float(sampleIndex);
    float angle = sampleVal * goldenAngle + phi;
    return vec2(cos(angle), sin(angle)) * sqrt(sampleVal + 0.5) / sqrt(float(numSamples));
}

float pssm(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {
    float beavis = 0.01;
    int sampleCount = 32;
    float penumbraWidth = 0.002;

    const float PHI = 1.61803398874989484820459;
    float phi = 0; // 6.28 * fract(tan(distance(vpos.xy * 20.0 * PHI, vpos.xy * 20.0) * 0.01) * vpos.x);

    float cumulative = 0.;
    float weight = 0.;
    for (int s = 0; s < sampleCount; ++s) {
        vec2 offset = vogelDiskSample(s, sampleCount, phi) * penumbraWidth;
        vec2 uv = vshadow.xy + offset;
        float shadowSample = texture(shadowTexture, uv).r;
        float val = (shadowSample > 0.00 && shadowSample < vshadow.z - beavis && uv.x > 0. && uv.x < 1. && uv.y > 0. && uv.y < 1.) ? 1. : 0.;
        cumulative += val;
        weight += 1.;
    }
    return cumulative / weight;
}

float hard(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {
    float beavis = 0.002;
    float shadowSample = texture(shadowTexture, vshadow.xy).r;
    return (shadowSample > 0.001 && shadowSample < vshadow.z - beavis && vshadow.x > 0.001 && vshadow.x < 0.999 && vshadow.y > 0.001 && vshadow.y < 0.999 && vshadow.z < 0.999) ? 1. : 0.;
}

float shadow(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {
    #ifdef PSSM_SHADOW
        return pssm(shadowTexture, vshadow, vpos);
    #endif
    #ifdef VSM_SHADOW
        return vsm(shadowTexture, vshadow, vpos);
    #endif
    #ifdef HARD_SHADOW
        return hard(shadowTexture, vshadow, vpos);
    #endif
    return 0.;
}