#import "lib/header.glsl"

in vec3 pos;
in vec3 normal;
in vec2 tex;

out vec3 mpos;
out vec3 mnormal;
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

const mat4 biasMatrix = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 0.5, 0.0,
    0.5, 0.5, 0.5, 1.0
);

void main() {

    vec4 worldPos = model * vec4(pos, 1.0);

    mpos = pos;
    mnormal = normal;
    vpos = worldPos.xyz;
    vtex = tex;
    vnormal = mat3(transpose(inverse(model))) * normal;

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