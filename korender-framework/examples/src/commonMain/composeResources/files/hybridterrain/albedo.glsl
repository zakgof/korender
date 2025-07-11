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

vec4 pluginAlbedo() {

    vec3 sand = vec3(0.89, 0.79, 0.46) + 0.3 * sin(fbm(vtex *  64.0));
    vec3 grass = vec3(0.2, 0.6, 0.3);
    vec3 snow = vec3(0.9, 0.9, 0.9);
    vec3 rock = vec3(0.5, 0.4, 0.4);


    vec2 uvnoise = vec2(0.01 * fbm(vtex * 4.));
    float patchSample = texture(patchTexture, vtex + uvnoise).r * 255;
    int patchIndex = int(patchSample);
    float power = fract(patchSample);

    vec3 color = sand; // 0
    switch (patchIndex) {
        case 1: color = vec3(0.2, 0.6, 0.3); break;
        case 2: color = vec3(0.2, 0.7, 0.2); break;
        case 3: color = vec3(0.3, 0.8, 0.2); break;
        case 10: color = vec3(0.5, 0.4, 0.4); break;
        case 20: color = vec3(0.9, 0.9, 0.9); break;
    }

    vec4 sdfSample = texture(sdf, vtex);
    if (sdfSample.r < -0.6) {
        color = textureGrad(road, vec2(0.5 - 0.5/0.6 * sdfSample.r, sdfSample.g * 0.1f),
        dFdx(vtex)*0.01*0.3, dFdy(vtex)*0.01*0.3 ).rgb;
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