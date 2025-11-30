#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

#uniforms

out vec4 fragColor;

#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"
#import "$sky"

#ifdef PLUGIN_SECSKY
#import "$secsky"
#endif

void main() {

    vec3 look = screenToLook(vtex);
    vec3 color = sky(look, 0.);

    #ifdef PLUGIN_SECSKY
        color = pluginSecsky(look, color);
    #endif

    fragColor = vec4(color, 1.);

    vec4 v = vec4(0.0, 0.0, -2.0 , 1.0);

    vec3 w = (inverse(view) * v).xyz;

//
//    if (look.z < -0.9)
//        fragColor = vec4(1.0, 0.0, 0.0, 0.999);
}