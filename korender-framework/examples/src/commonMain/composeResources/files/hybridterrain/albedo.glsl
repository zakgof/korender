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
        case 1: color = texture(grassTexture, uv * 8.0).rgb + vec3(0.1, 0.4, 0.0) * fbm(uv * 3.0);
            break;
        case 2: color = vec3(0.9, 0.9, 0.9); break;
        case 3: color = vec3(0.9, 0.3, 0.4); break;
    }
    return color;
}


vec4 pluginAlbedo() {

    vec4 materialSample = texture(patchTexture, vtex + vec2(-0.005 + 0.01 * fbm(vtex * 64.), -0.005 + 0.01 * fbm(vtex.yx * 63.)));
    vec3 color = vec3(0.);
    for (int i=0; i<4; ++i) {
        color += colorAtIndex(i, vtex) * materialSample[i];
    }

    vec4 sdfSample = texture(sdf, vtex);
    float sdfCross = (sdfSample.r - 0.3) / (1. - 2. * 0.3);
    if (sdfCross > 0. && sdfCross < 1. && sdfSample.b >= 1.0) {
        color = texture(road, vec2(sdfCross, sdfSample.g * 0.05f)).rgb;
    }

    return vec4(color, 1.0);
}