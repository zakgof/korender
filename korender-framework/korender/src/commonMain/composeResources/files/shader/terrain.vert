#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 2) in vec2 tex;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 view;
uniform mat4 projection;

uniform vec3 tileOffsetAndScale;
uniform sampler2D heightTexture;
uniform float antipop;

const float heightScale = 2.3;
const float nstep = 1.0 / 8192.0;

float h(vec2 uv) {
    vec4 smpl = texture(heightTexture, uv);
    return (smpl.r * 255.0 + smpl.g) * heightScale - 4.;
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

void main() {
    vpos.xz = tileOffsetAndScale.xy * 32.0 + pos.xz * tileOffsetAndScale.z;
    vtex = (vpos.xz + 0.5) / 8192.0;
    vpos.y = h(vtex);
    int xx = int(round(pos.x) + 0.01);
    int zz = int(round(pos.z) + 0.01);
    if ((xx % 2) == 0 && (zz % 2) == 1) {
        float step = tileOffsetAndScale.z / 8192.0;
        float hD = h(vtex + vec2(0.,  step));
        float hU = h(vtex + vec2(0., -step));
        vpos.y = mix((hD + hU) * 0.5, h(vtex), antipop);
    }
    if ((xx % 2) == 1 && (zz % 2) == 0) {
        float step = tileOffsetAndScale.z / 8192.0;
        float hL = h(vtex + vec2(-step, 0.));
        float hR = h(vtex + vec2( step, 0.));
        vpos.y = mix((hL + hR) * 0.5, h(vtex), antipop);
    }
    if ((xx % 2) == 1 && (zz % 2) == 1) {
        float step = tileOffsetAndScale.z / 8192.0;
        float hL = h(vtex + vec2( step,  step));
        float hR = h(vtex + vec2(-step, -step));
        vpos.y = mix((hL + hR) * 0.5, h(vtex), antipop);
    }
    vnormal = vec3(0. ,1., 0.);
    gl_Position = projection * (view * vec4(vpos, 1.));
}