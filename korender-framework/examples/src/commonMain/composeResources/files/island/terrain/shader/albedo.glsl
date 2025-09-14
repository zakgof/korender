#import "!shader/lib/noise.glsl"

uniform sampler2D patchTexture;

uniform sampler2D sdf;
uniform sampler2D road;

uniform sampler2D grassTexture;

uniform sampler2D runwayTexture;

#uniform vec2 runwayP1;
#uniform vec2 runwayP2;

vec3 colorAtIndex(int patchIndex, vec2 uv) {
    vec3 color = vec3(0.1);
    switch (patchIndex) {
        case 0: color = vec3(0.796, 0.741, 0.576) + vec3(fbm(uv * 96.0) * 0.2);
            break;
        case 1: color = texture(grassTexture, uv * 64.0).rgb + vec3(0.1, 0.4, 0.0) * fbm(uv * 3.0);
            break;
        case 2: color = vec3(0.9, 0.9, 0.9); break;
        case 3: color = vec3(0.9, 0.3, 0.4); break;
    }
    return color;
}

vec2 toRoi(vec2 uv) {
    float width = 0.03;
    vec2 v = runwayP2 - runwayP1;
    float height = length(v);
    vec2 v_hat = v / height;
    vec2 u_hat = vec2(-v_hat.y, v_hat.x);
    return vec2(
        -dot(uv - runwayP1, u_hat) / width + 0.5,
        dot(uv - runwayP1, v_hat) / height
    );
}

vec4 pluginAlbedo() {

    vec4 materialSample = texture(patchTexture, vtex + vec2(-0.005 + 0.01 * fbm(vtex * 64.), -0.005 + 0.01 * fbm(vtex.yx * 63.)));
    vec3 color = vec3(0.);

    color += colorAtIndex(0, vtex) * materialSample.r;
    color += colorAtIndex(1, vtex) * materialSample.g;
    color += colorAtIndex(2, vtex) * materialSample.b;
    color += colorAtIndex(3, vtex) * materialSample.a;

    vec4 sdfSample = texture(sdf, vtex);
    float sdfCross = (sdfSample.r - 0.35) / (1. - 2. * 0.35);
    if (sdfCross > 0. && sdfCross < 1. && sdfSample.b >= 1.0) {
        color = texture(road, vec2(sdfCross, sdfSample.g * 5.0f)).rgb;
    }

    vec2 roiTex = toRoi(vtex);
    if (roiTex.x > 0. && roiTex.x < 1. && roiTex.y > 0. && roiTex.y < 1.) {
        color = texture(runwayTexture, roiTex).xyz;
    }

    return vec4(color, 1.0);
}