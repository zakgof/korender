#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 2) in vec2 tex;
layout(location = 7) in float phi;

uniform mat4 view;
uniform mat4 projection;
uniform float time;
uniform float grassCutoffDepth;
uniform vec3 cameraPos;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
out vec4 vcolor;
out float vocclusion;

#import "!shader/lib/noise.glsl"

void main() {

    vec3 color1 = vec3(0.60, 0.64, 0.31);
    vec3 color2 = vec3(0.40, 0.44, 0.21);

    float PHI = 1.618034;
    float r1 = fract(tan(distance(33.13*pos.xz*PHI, 32.98*pos.xz)*0.13)*pos.x);
    float r2 = fract(tan(distance(31.22*pos.xz*PHI, 33.41*pos.xz)*0.43)*pos.z);

    float w = 0.1;
    float len = 1.3 + 0.3 * r2;

    float rot = (r1+r2) * 6.28;


    vec2 wind = vec2(1.4, 0.0)
                + vec2(sin(r1 * 4.0 + time * (1.2 + r1)),
                       sin(r2 * 4.0 + time * (1.2 + r2))) * 0.3;

    float fade = 1.0 - smoothstep(grassCutoffDepth * 0.60, grassCutoffDepth * 1.00, length(cameraPos - pos));
    len *= fade;
    w *= fade;
    wind *= fade;

    vec2 stilln = vec2(-sin(rot), cos(rot));
    float bend = dot(stilln, wind);

    float offset = tex.y * tex.y * bend;
    float theta = atan(2.0 * tex.y * bend, len);

    vec3 n = vec3(stilln.x * cos(theta), sin(theta), stilln.y * cos(theta)) * float(1 - phi * 2);
    vnormal = mix(n, vec3(0., 1., 0.), fade);

    vpos = pos +
        vec3(r2 * 0.2, 0.0, r1 * 0.2) +
        vec3(w * tex.x * cos(rot), len * tex.y, w * tex.x * sin(rot)) -
        vec3(stilln.x, 0.3 * tex.y, stilln.y) * offset;

    vtex = tex;
    vcolor = vec4(mix(color1, color2, r1 * r2), 1.0);
    vocclusion = tex.y * tex.y;
    gl_Position = projection * (view * vec4(vpos, 1.0));
}