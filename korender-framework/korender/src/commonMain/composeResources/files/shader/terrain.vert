#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 8) in int b1;
layout(location = 9) in int b2;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

#uniform float cell;
#uniform vec3 tileOffsetAndScale;
#uniform vec3 antipop;
#uniform float antipopSpan;

#uniforms

#import "$terrain"

#import "$vprojection"

void main() {

    vec2 intpos = tileOffsetAndScale.xy + vec2(float(b1), float(b2)) * tileOffsetAndScale.z;

    vpos.xz = intpos * cell + pluginTerrainCenter().xz;

    float reso = float(pluginTerrainTextureSize());

    vtex = intpos / reso + vec2(0.5);
    float step = tileOffsetAndScale.z / reso;

    float h = pluginTerrainHeight(vtex);
    vpos.y = h;

    float w =
        smoothstep(antipop.x * 2.0, antipop.x * 2.0 + antipopSpan, float(b1)) *
        smoothstep(antipop.y * 2.0, antipop.y * 2.0 + antipopSpan, float(b2)) *
        smoothstep((1.0-antipop.x) * 2.0, (1.0-antipop.x) * 2.0 + antipopSpan, antipop.z-float(b1)) *
        smoothstep((1.0-antipop.y) * 2.0, (1.0-antipop.y) * 2.0 + antipopSpan, antipop.z-float(b2));

    if ((b1 % 2) == 0 && (b2 % 2) == 1) {
        float hD = pluginTerrainHeight(vtex + vec2(0.,  step));
        float hU = pluginTerrainHeight(vtex + vec2(0., -step));
        vpos.y = mix((hD + hU) * 0.5, h, w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 0) {
        float hL = pluginTerrainHeight(vtex + vec2(-step, 0.));
        float hR = pluginTerrainHeight(vtex + vec2( step, 0.));
        vpos.y = mix((hL + hR) * 0.5, h, w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 1) {
        float hL = pluginTerrainHeight(vtex + vec2(step,  step));
        float hR = pluginTerrainHeight(vtex + vec2(-step, -step));
        vpos.y = mix((hL + hR) * 0.5, h, w);
    }
    vnormal = vec3(0. ,1., 0.);

    vec3 viewPos = (view * vec4(vpos, 1.0)).xyz;
    gl_Position = pluginVProjection(viewPos);
}