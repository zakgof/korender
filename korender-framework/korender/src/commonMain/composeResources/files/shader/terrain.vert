#import "!shader/lib/header.glsl"


layout(location = 7) in float w;
layout(location = 8) in int b1;
layout(location = 9) in int b2;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 view;
uniform mat4 projection;

uniform vec3 tileOffsetAndScale;
uniform sampler2D heightTexture;
uniform sampler2D fbmTexture;
uniform float antipop;

#import "!shader/lib/terrain.glsl"

void main() {
    float step = tileOffsetAndScale.z / 8192.0;

    vpos.xz = tileOffsetAndScale.xy + vec2(float(b1), float(b2)) * tileOffsetAndScale.z;
    vtex = vpos.xz / 8192.0;
    vpos.y = h(vtex);

    if ((b1 % 2) == 0 && (b2 % 2) == 1) {
        float hD = h(vtex + vec2(0.,  step));
        float hU = h(vtex + vec2(0., -step));
        vpos.y = mix((hD + hU) * 0.5, h(vtex), w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 0) {
        float hL = h(vtex + vec2(-step, 0.));
        float hR = h(vtex + vec2( step, 0.));
        vpos.y = mix((hL + hR) * 0.5, h(vtex), w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 1) {
        float hL = h(vtex + vec2(step,  step));
        float hR = h(vtex + vec2(-step, -step));
        vpos.y = mix((hL + hR) * 0.5, h(vtex), w);
    }
    vnormal = vec3(0. ,1., 0.);
    gl_Position = projection * (view * vec4(vpos, 1.));
}