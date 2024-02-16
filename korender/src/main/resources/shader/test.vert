#version 140

in vec3 pos;
in vec3 normal;
in vec2 tex;

out vec3 mpos;
out vec3 mnormal;
out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
#ifdef SHADOW_RECEIVER
out vec3 vshadow;
#endif

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
#ifdef SHADOW_RECEIVER
  uniform mat4 shadowView;
  uniform mat4 shadowProjection;
#endif

#ifdef SHADOW_RECEIVER
const mat4 biasMatrix = mat4(
    0.5, 0.0, 0.0, 0.0,
    0.0, 0.5, 0.0, 0.0,
    0.0, 0.0, 0.5, 0.0,
    0.5, 0.5, 0.5, 1.0
);
#endif

void main() {

    vec4 worldPos = model * vec4(pos, 1.0);

    mpos = pos;
    mnormal = normal;
    vpos = worldPos.xyz;
    vtex = tex;
    vnormal = mat3(transpose(inverse(model))) * normal;

    #ifdef SHADOW_RECEIVER
	  vshadow = (biasMatrix * shadowProjection * shadowView * worldPos).xyz;
    #endif

    gl_Position = projection * (view * worldPos);
}