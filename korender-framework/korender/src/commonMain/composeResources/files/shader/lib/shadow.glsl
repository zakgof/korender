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

float calculateShadow(int i, vec3 v, int mode) {
    #ifndef OPENGL
    float sh = 0.;
    switch (i) {
        case 0: sh =  shadow(shadowTextures[0],  i, v, mode); break;
        case 1: sh =  shadow(shadowTextures[1],  i, v, mode); break;
        case 2: sh =  shadow(shadowTextures[2],  i, v, mode); break;
        case 3: sh =  shadow(shadowTextures[3],  i, v, mode); break;
        case 4: sh =  shadow(shadowTextures[4],  i, v, mode); break;
        case 5: sh =  shadow(shadowTextures[5],  i, v, mode); break;
        case 6: sh =  shadow(shadowTextures[6],  i, v, mode); break;
        case 7: sh =  shadow(shadowTextures[7],  i, v, mode); break;
        case 8: sh =  shadow(shadowTextures[8],  i, v, mode); break;
        case 9: sh =  shadow(shadowTextures[9],  i, v, mode); break;
        case 10: sh = shadow(shadowTextures[10], i, v, mode); break;
        case 11: sh = shadow(shadowTextures[11], i, v, mode); break;
    }
    return sh;
    #else
    return shadow(shadowTextures[i], i, v, mode);
    #endif
}