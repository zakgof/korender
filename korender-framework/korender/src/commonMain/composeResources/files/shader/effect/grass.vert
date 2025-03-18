#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 2) in vec2 tex;
layout(location = 7) in float phi;

uniform mat4 view;
uniform mat4 projection;
uniform float time;
uniform vec3 cameraPos;

uniform float grassCutoffDepth;
uniform float grassCell;

uniform vec3 grassColor1;
uniform vec3 grassColor2;
uniform float bladeWidth;
uniform float bladeLength;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
out vec4 vcolor;
out float vocclusion;

#import "!shader/lib/noise.glsl"

void main() {

    float PHI = 1.618034;
    float r1 = fract(tan(distance(3.13*pos.xz*PHI, 3.98*pos.xz)*0.13)*pos.x);
    float r2 = fract(tan(distance(3.22*pos.xz*PHI, 3.41*pos.xz)*0.43)*pos.z);

    float w = bladeWidth * (1.0 + (0.3 * r1));
    float len = bladeLength * (1.0 + (0.3 * r2));

    float rot = (r1+r2) * 6.28;


    vec2 wind = vec2(0.7, 0.0)
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
        vec3(r2 * grassCell * 0.5, 0.0, r1 * grassCell * 0.5) +
        vec3(w * tex.x * cos(rot), len * tex.y, w * tex.x * sin(rot)) -
        vec3(stilln.x, 0.3 * tex.y, stilln.y) * offset;

    vtex = tex;
    vcolor = vec4(mix(grassColor1, grassColor2, r1 * r2), 1.0);
    vocclusion = tex.y * tex.y;
    gl_Position = projection * (view * vec4(vpos, 1.0));
}