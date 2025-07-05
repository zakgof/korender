#import "!shader/lib/noise.glsl"

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

    float sandW = 1.0 - smoothstep(40.0, 70.0, vpos.y - 20.0 * fbm(vtex * 18.0));
    float snowW = smoothstep(100.0, 200.0, vpos.y - 96.0 * fbm(vtex * 4.0));
    float rockW = smoothstep(-0.45, -0.44, -vnormal.y);
    float grassW = clamp(1.0 - sandW - rockW - snowW, 0.0, 1.0);

    vec3 color = (sand * sandW +
    snow * snowW +
    rock * rockW +
    grass * grassW) / (sandW + snowW + rockW + grassW);


    float c = 0.25 +
    fbm(vtex *  64.0) * 0.5 +
    fbm(vtex * 128.0) * 0.25;

    vec4 roiColor = roi(vtex);

    vec4 tx = texture(sdf, vtex);
    float v = (tx.r - 0.5) * 2.0 * 6.0 / 128.0;

    vec4 finalColor = vec4(mix(color + 0.25, roiColor.rgb, roiColor.a), 1.0);
    if (abs(v) < 0.001) {
        finalColor = texture(road, vec2(0.5 + v / 0.001, tx.b));
    }

    return finalColor;
}