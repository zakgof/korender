const float heightScale = 512.0;
const float nstep = 1.0 / 8192.0;

float h(vec2 uv) {

    if (uv.x < 0. || uv.x > 1. || uv.y < 0. || uv.y > 1.)
        return -1000.;

    float base = texture(heightTexture, uv).r * heightScale;

    float o128 = texture(fbmTexture, uv * 128.0).r - 0.5;
    float o32 = texture(fbmTexture, uv * 32.0).r - 0.5;
    float o8 = texture(fbmTexture, uv * 8.0).r - 0.5;
    float o2 = textureLod(fbmTexture, uv * 1.0, 4.0).r - 0.5;

    // return 100.0  + 100.0 * sin(uv.x * 100.0) * cos (uv.y * 100.0);


    return base - 64.0
        + o128 * 8.0
        + o32 * 16.0
        + max(base-64., 0.05) * o8 *  0.4
        + max(base-80., 0.1) * o2 *  1.6
    ;
}

vec3 n(vec2 uv) {

    float hL = h(uv + vec2(-nstep, 0.));
    float hR = h(uv + vec2(nstep, 0.));
    float hD = h(uv + vec2(0, -nstep));
    float hU = h(uv + vec2(0, nstep));

    vec3 dx = normalize(vec3(2.0,  hR - hL, 0.0));
    vec3 dz = normalize(vec3(0.0,  hU - hD, 2.0));

    return normalize(cross(dz, dx));
}