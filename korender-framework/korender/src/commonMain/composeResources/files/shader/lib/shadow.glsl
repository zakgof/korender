float vsm(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {

    #ifdef WEBGL
    const float bias = 0.005;
    const float varianceMagic = 0.001;
    #else
    const float bias = 0.005;
    const float varianceMagic = 0.00001;
    #endif

    if (vshadow.x < 0.001 || vshadow.x > 0.999 || vshadow.y < 0.001 || vshadow.y > 0.999)
        return 0.;

    vec3 moments = texture(shadowTexture, vshadow.xy).rgb;
    float p = vshadow.z * vshadow.z - moments.x;

    float hardStep = smoothstep(0., bias, p);
    float variance = moments.y - moments.x * moments.x;

    variance = max(variance, varianceMagic);
    float hardness =  p * p / (variance + p * p);

    return hardStep * hardness;
}


vec2 vogelDiskSample(int sampleIndex, int numSamples, float phi) {
    float goldenAngle = 2.39996323;
    float sampleVal = float(sampleIndex);
    float angle = sampleVal * goldenAngle + phi;
    return vec2(cos(angle), sin(angle)) * sqrt(sampleVal + 0.5) / sqrt(float(numSamples));
}

float pccf(sampler2D shadowTexture, vec3 vshadow, vec3 vpos) {
    float beavis = 0.01;
    int sampleCount = 32;
    float penumbraWidth = 0.001;

    const float PHI = 1.61803398874989484820459;
    float phi = 0.; // 6.28 * fract(tan(distance(vpos.xy * 20.0 * PHI, vpos.xy * 20.0) * 0.01) * vpos.x);

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

    return (shadowSample < vshadow.z - beavis
        && vshadow.x > 0.001 && vshadow.x < 0.999
        && vshadow.y > 0.001 && vshadow.y < 0.999) ? 1. : 0.;
}

float shadow(sampler2D shadowTexture, vec3 vshadow, vec3 vpos, int mode) {
    switch (mode) {
        case 0: return hard(shadowTexture, vshadow, vpos);
        case 1: return pccf(shadowTexture, vshadow, vpos);
        case 2: return vsm(shadowTexture, vshadow, vpos);
    }
    return 0.;
}