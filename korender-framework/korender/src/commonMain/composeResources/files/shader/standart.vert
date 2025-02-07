#import "!shader/lib/header.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;

#ifdef SKINNING
layout(location = 3) in uvec4 joints;
layout(location = 4) in vec4 weights;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

#ifdef SKINNING
    const int MAX_JOINTS = 32;
    uniform mat4 jntMatrices[MAX_JOINTS];
#endif

#ifdef HEMISPHERE
    #import "!shader/lib/sky.glsl"
#endif

void main() {

#ifdef SKINNING
    mat4 skinningMatrix =
        weights.x * jntMatrices[joints.x] +
        weights.y * jntMatrices[joints.y] +
        weights.z * jntMatrices[joints.z] +
        weights.w * jntMatrices[joints.w];

    vec4 worldPos = (skinningMatrix * vec4(pos, 1.0));
    vnormal = mat3(transpose(inverse(skinningMatrix))) * normal;
#else
    vec4 worldPos = model * vec4(pos, 1.0);
    vnormal = mat3(transpose(inverse(model))) * normal;
#endif

    vpos = worldPos.xyz;
    vtex = tex;

#ifdef HEMISPHERE
    #ifdef HTOP
        vec2 xy = skydiskfromlook(vpos, 1.5) * 2.0 - vec2(1.0);
        float z = length(vpos) * vpos.y / (1000.0 * abs(vpos.y)) * 2.0 - 1.0;
    #endif
    #ifdef HBOTTOM
        vec2 xy = skydiskfromlook(vec3(vpos.x, -vpos.y, vpos.z), 1.5) * 2.0 - vec2(1.0);
        float z = -length(vpos) * vpos.y / (1000.0 * abs(vpos.y)) * 2.0 - 1.0;   // -1..1
    #endif
    gl_Position = vec4(xy, z, 1.0);
#else
    gl_Position = projection * (view * worldPos);
#endif

}