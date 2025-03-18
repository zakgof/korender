#import "!shader/lib/header.glsl"

layout(location = 8) in int b1;
layout(location = 9) in int b2;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 view;
uniform mat4 projection;

uniform float cell;
uniform vec3 tileOffsetAndScale;
uniform vec3 antipop;
uniform vec3 terrainCenter;

uniform sampler2D heightTexture;
uniform int heightTextureSize;
uniform float heightScale;
uniform float outsideHeight;
uniform float antipopSpan;

#import "!shader/lib/terrain.glsl"

void main() {

    vec2 intpos = tileOffsetAndScale.xy + vec2(float(b1), float(b2)) * tileOffsetAndScale.z;

    vpos.xz = intpos * cell + terrainCenter.xz;

    vtex = intpos / float(heightTextureSize) + vec2(0.5);
    float step = tileOffsetAndScale.z / float(heightTextureSize);

    float h = heightAt(vtex);
    vpos.y = h;

    float w =
        smoothstep(antipop.x * 2.0, antipop.x * 2.0 + antipopSpan, float(b1)) *
        smoothstep(antipop.y * 2.0, antipop.y * 2.0 + antipopSpan, float(b2)) *
        smoothstep((1.0-antipop.x) * 2.0, (1.0-antipop.x) * 2.0 + antipopSpan, antipop.z-float(b1)) *
        smoothstep((1.0-antipop.y) * 2.0, (1.0-antipop.y) * 2.0 + antipopSpan, antipop.z-float(b2));

    if ((b1 % 2) == 0 && (b2 % 2) == 1) {
        float hD = heightAt(vtex + vec2(0.,  step));
        float hU = heightAt(vtex + vec2(0., -step));
        vpos.y = mix((hD + hU) * 0.5, h, w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 0) {
        float hL = heightAt(vtex + vec2(-step, 0.));
        float hR = heightAt(vtex + vec2( step, 0.));
        vpos.y = mix((hL + hR) * 0.5, h, w);
    }
    if ((b1 % 2) == 1 && (b2 % 2) == 1) {
        float hL = heightAt(vtex + vec2(step,  step));
        float hR = heightAt(vtex + vec2(-step, -step));
        vpos.y = mix((hL + hR) * 0.5, h, w);
    }
    vnormal = vec3(0. ,1., 0.);
    gl_Position = projection * (view * vec4(vpos, 1.));
}