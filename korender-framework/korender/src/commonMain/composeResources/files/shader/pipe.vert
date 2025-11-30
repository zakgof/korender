#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;
layout(location = 5) in vec2 scale;
#ifdef INSTANCING
    layout(location = 11) in vec4 instanceModel0;
    layout(location = 12) in vec4 instanceModel1;
    layout(location = 13) in vec4 instanceModel2;
    layout(location = 14) in vec4 instanceModel3;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

out vec3 vleft;
out vec2 vscale;

#uniform mat4 model;
#uniforms

#import "$vprojection"

void main() {

    mat4 totalModel = model;

    #ifdef INSTANCING
        totalModel = model * mat4(instanceModel0, instanceModel1, instanceModel2, instanceModel3);
    #endif

    vec3 basepos = (totalModel * vec4(pos, 1.0)).xyz;
    vnormal = mat3(transpose(inverse(totalModel))) * normal;

    vec3 toEye = normalize(cameraPos - basepos);

    vleft = normalize(cross(toEye, vnormal));
    float width = mix(scale.x, scale.y, tex.y);

    vec3 back = normalize(cross(vnormal, vleft));

    vpos = basepos - vleft * (tex.x - 0.5) * 2.0 * width + vnormal * tex.y + back * width;
    vtex = tex;
    vscale = scale;

    vec4 worldPos = vec4(vpos, 1.0);

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}