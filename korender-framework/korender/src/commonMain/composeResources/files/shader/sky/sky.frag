#import "!shader/lib/header.glsl"
#import "!shader/lib/sky.glsl"

#import "$sky"

#ifdef PLUGIN_SECSKY
    #import "$secsky"
#endif

in vec2 vtex;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragColor;

void main() {

#ifdef HEMISPHERE
    #ifdef HTOP
        vec3 look = lookfromskydisk(vtex, 1.5);
    #endif
    #ifdef HBOTTOM
        vec3 look = lookfromskydisk(vtex, 1.5);
        look.y = -look.y;
    #endif
#else
    vec3 look = screentolook(vtex, projection * view, cameraPos);
#endif

    vec3 color = sky(look);

    #ifdef PLUGIN_SECSKY
        color = pluginSecsky(look, color);
    #endif

    fragColor = vec4(color, 1.);
}