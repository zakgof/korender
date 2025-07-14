#import "!shader/lib/noise.glsl"

uniform sampler2D patchTexture;
uniform sampler2D roiTextures[2];
uniform vec3[2] roiuvs;
uniform int roiCount;

vec4 roi(vec2 uv) {
    vec4 color = vec4(0.);
    //    for (int r=0; r<roiCount; r++) {
    //        vec3 r3 = roiuvs[r];
    //        vec2 roiuv = (uv - r3.xy) / r3.z;
    //        if (roiuv.x >= 0.0 && roiuv.x <= 1.0 && roiuv.y >= 0.0 && roiuv.y <= 1.0) {
    //            color += texture(roiTextures[r], roiuv);
    //        }
    //    }
    return color;
}

uniform sampler2D sdf;
uniform sampler2D road;

uniform sampler2D grassTexture;


vec3 colorAtIndex(float index, vec2 uv) {
    int patchIndex = int(index);
    vec3 color = vec3(0.1);
    switch (patchIndex) {
        case 0: color = vec3(0.796, 0.741, 0.576) + vec3(fbm(uv * 35.0) * 0.2);
            break;

        case 1: color = vec3(0.2, 0.6, 0.3); break;
        case 2: color = vec3(0.2, 0.7, 0.2); break;
        case 3: color = vec3(0.3, 0.8, 0.2); break;
        case 10: color = texture(grassTexture, uv * 8.0).rgb + vec3(0.1, 0.4, 0.0) * fbm(uv * 3.0);
            break;
        case 20: color = vec3(0.9, 0.9, 0.9); break;
    }
    return color;
}

vec2 vogel(int sampleIndex, int numSamples, float phi) {
    float goldenAngle = 2.39996323;
    float sampleVal = float(sampleIndex);
    float angle = sampleVal * goldenAngle + phi;
    return vec2(cos(angle), sin(angle)) * sqrt(sampleVal + 0.5) / sqrt(float(numSamples));
}

vec4 pluginAlbedo() {

    float centerIndex = texture(patchTexture, vtex).r * 255.0;

    vec3 color = vec3(0.0);
    float sumWeight = 0.0;

    for (int i = 0; i < 4; ++i) {

        vec2 offsetUV = vtex + vogel(i, 4, vtex.x * 1000. * vtex.y * 880.) * (1.0 / 512.0) * 2.0;

        // Sample neighbor index
        float neighborIndex = texture(patchTexture, offsetUV).r * 255.0;

        // Weight: higher if the index is similar to center
        float weight = 1.0 - min(abs(centerIndex - neighborIndex) / 255.0, 1.0);

        // Alternatively, you can clamp to 0.5 if you prefer less cross-fade:
        // float weight = 1.0 - smoothstep(0.0, 10.0, abs(centerIndex - neighborIndex));

        // Sample the albedo
        vec3 neighborColor = colorAtIndex(neighborIndex, vtex);

        color += neighborColor * weight;
        sumWeight += weight;
    }

    color /= sumWeight;


    vec4 sdfSample = texture(sdf, vtex);
    float sdfCross = (sdfSample.r - 0.3) / (1. - 2. * 0.3);
    if (sdfCross > 0. && sdfCross < 1. && sdfSample.b >= 1.0) {
        color = texture(road, vec2(sdfCross, sdfSample.g * 0.05f)).rgb;
    }
    // color = sdfSample.rgb;

    //        finalColor = texture(road, vec2(0.5 + v / 0.001, tx.b));
    //    }

    //    vec4 finalColor = vec4(mix(color + 0.25, roiColor.rgb, roiColor.a), 1.0);
    //    if (abs(v) < 0.001) {
    //        finalColor = texture(road, vec2(0.5 + v / 0.001, tx.b));
    //    }


    return vec4(color, 1.0);
}