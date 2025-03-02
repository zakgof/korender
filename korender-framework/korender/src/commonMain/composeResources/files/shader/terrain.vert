#import "!shader/lib/header.glsl"


layout(location = 8) in int b1;
layout(location = 9) in int b2;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
out vec3 code;

uniform mat4 view;
uniform mat4 projection;

uniform vec3 tileOffsetAndScale;
uniform float px;
uniform float pz;

uniform sampler2D heightTexture;
uniform sampler2D fbmTexture;

#import "!shader/lib/terrain.glsl"

void main() {
    float step = tileOffsetAndScale.z / 8192.0;

    vpos.xz = tileOffsetAndScale.xy + vec2(float(b1), float(b2)) * tileOffsetAndScale.z;
    vtex = (vpos.xz + 0.5) / 8192.0;
    vpos.y = h(vtex);

    float w =
        smoothstep(px * 2.0, px * 2.0 + 3.0, float(b1)) *
        smoothstep(pz * 2.0, pz * 2.0 + 3.0, float(b2)) *
        smoothstep((1.0-px) * 2.0, (1.0-px) * 2.0 + 3.0, float(46-b1)) *
        smoothstep((1.0-pz) * 2.0, (1.0-pz) * 2.0 + 3.0, float(46-b2));

    code = vec3(w, 0, 0);
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