#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 2) in vec2 tex;
layout(location = 7) in float phi;

uniform mat4 view;
uniform mat4 projection;
uniform float time;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
out vec4 vcolor;

#import "!shader/lib/noise.glsl"

void main() {

    float PHI = 1.618034;
    float r1 = fract(tan(distance(3.0*pos.xz*PHI, 3.0*pos.xz)*0.13)*pos.x);
    float r2 = fract(tan(distance(3.0*pos.xz*PHI, 3.0*pos.xz)*0.43)*pos.x);

    float w = 0.1;
    float len = 1.3 + 0.3 * r2;
    float rot = r1 * 6.28;

    vec2 wind = vec2(0.5, 0.5f)
                + vec2(sin(r1*4.0 + time * (2.4 + r1)),
                       sin(r2*4.0 + time * (2.2 + r2))) * 0.3;

    vec2 stilln = vec2(-sin(rot), cos(rot));
    float bend = dot(stilln, wind);

    float offset = tex.y * tex.y * bend;
    float theta = atan(len, 2 * tex.y * bend);

    vnormal = vec3(stilln.x * sin(theta), cos(theta), stilln.y * sin(theta));

    vpos = pos +
        vec3(w * tex.x * cos(rot), len * tex.y, w * tex.x * sin(rot)) -
        vnormal * offset;

    vtex = tex;
    vcolor = vec4(0.3 + 0.5 * r1, 1.0, 0.1 + 0.2 * r2, 1.0);
    gl_Position = projection * (view * vec4(vpos, 1.0));
}