const float heightScale = 5.0;
const float nstep = 1.0 / 8192.0;

float h(vec2 uv) {
    vec4 smpl = textureLod(heightTexture, uv, 2);
    float m = (smpl.r * 255.0 + smpl.g) * heightScale;

    float noi =
        pow(texture(fbmTexture, uv * 0.25).r + 0.2, 12.0) * 1024.0 +
            texture(fbmTexture, uv * 1.00).r              * 512.0 +
            texture(fbmTexture, uv * 4.0).r               * 128.0;

    float w = noi * 0.001;

    // noi + w * texture(fbmTexture, uv * 16.0).r * 32.0;


    return m
        + texture(fbmTexture, uv * 64.0).r * 2.0
        + texture(fbmTexture, uv * 16.0).r * 8.0
        + smoothstep(150, 250, m) * texture(fbmTexture, uv * 4.0).r * 128.0
        + smoothstep(100, 300, m) * texture(fbmTexture, uv * 1.0).r * 128.0;

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