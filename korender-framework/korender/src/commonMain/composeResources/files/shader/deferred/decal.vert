#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 0) in vec3 pos;

#ifdef INSTANCING
    layout(location = 11) in vec4 instanceModel0;
    layout(location = 12) in vec4 instanceModel1;
    layout(location = 13) in vec4 instanceModel2;
    layout(location = 14) in vec4 instanceModel3;
#endif

out vec3 vpos;

#ifdef INSTANCING
    out mat4 model;
#endif

#ifndef INSTANCING
    #uniform mat4 model;
#endif

#uniforms

#import "$vprojection"

void main() {

    #ifdef INSTANCING
        model = mat4(instanceModel0, instanceModel1, instanceModel2, instanceModel3);
    #endif

    vec4 worldPos = model * vec4(pos, 1.0);

    vec3 viewPos = (view * worldPos).xyz;
    gl_Position = pluginVProjection(viewPos);
}