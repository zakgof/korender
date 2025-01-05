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

#ifdef SHADOW_RECEIVER0
out vec3 vshadow0;
#endif
#ifdef SHADOW_RECEIVER1
out vec3 vshadow1;
#endif
#ifdef SHADOW_RECEIVER2
out vec3 vshadow2;
#endif

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
#ifdef SHADOW_RECEIVER0
  uniform mat4 shadowView0;
  uniform mat4 shadowProjection0;
#endif
#ifdef SHADOW_RECEIVER1
    uniform mat4 shadowView1;
    uniform mat4 shadowProjection1;
#endif
#ifdef SHADOW_RECEIVER2
    uniform mat4 shadowView2;
    uniform mat4 shadowProjection2;
#endif
#ifdef SKINNING
    uniform mat4 jointMatrices[64];
    uniform mat4 inverseBindMatrices[64];
#endif

const mat4 biasMatrix = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 0.5, 0.0,
    0.5, 0.5, 0.5, 1.0
);

void main() {

#ifdef SKINNING
    mat4 skinningMatrix =
        weights.x * jointMatrices[joints.x] * inverseBindMatrices[joints.x] +
        weights.y * jointMatrices[joints.y] * inverseBindMatrices[joints.y] +
        weights.z * jointMatrices[joints.z] * inverseBindMatrices[joints.z] +
        weights.w * jointMatrices[joints.w] * inverseBindMatrices[joints.w];

    vec4 worldPos = (skinningMatrix * vec4(pos, 1.0));
    vnormal = mat3(transpose(inverse(skinningMatrix))) * normal;
#else
    vec4 worldPos = model * vec4(pos, 1.0);
    vnormal = mat3(transpose(inverse(model))) * normal;
#endif

    vpos = worldPos.xyz;
    vtex = tex;

    #ifdef SHADOW_RECEIVER0
	  vshadow0 = (biasMatrix * shadowProjection0 * shadowView0 * worldPos).xyz;
    #endif
    #ifdef SHADOW_RECEIVER1
      vshadow1 = (biasMatrix * shadowProjection1 * shadowView1 * worldPos).xyz;
    #endif
    #ifdef SHADOW_RECEIVER2
      vshadow2 = (biasMatrix * shadowProjection2 * shadowView2 * worldPos).xyz;
    #endif

    gl_Position = projection * (view * worldPos);
}