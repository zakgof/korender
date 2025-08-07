#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;
layout(location = 1) in vec3 normal;
layout(location = 2) in vec2 tex;
layout(location = 5) in vec2 scale;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;

out vec3 vleft;
out vec2 vscale;

#uniforms
#uniform mat4 model;

#import "$vprojection"

void main() {


    vec3 basepos = (model * vec4(pos, 1.0)).xyz;
    vnormal = mat3(transpose(inverse(model))) * normal;

    vec3 toEye = normalize(cameraPos - basepos);

    vleft = normalize(cross(toEye, vnormal));
    float width = mix(scale.x, scale.y, tex.y);

    vpos = basepos - vleft * (tex.x - 0.5) * 2.0 * width + vnormal * tex.y;

    vtex = tex;
    vscale = scale; // TODO

    vec4 worldPos = vec4(vpos, 1.0);

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}