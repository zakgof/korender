const float heightScale = 2.3;
const float nstep = 1.0 / 8192.0;

float h(vec2 uv) {
    vec4 smpl = texture(heightTexture, uv);
    return (smpl.r * 255.0 + smpl.g) * heightScale;
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