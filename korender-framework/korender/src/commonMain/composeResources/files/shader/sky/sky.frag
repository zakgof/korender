#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;

out vec4 fragColor;

#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"
#import "$sky"

#ifdef PLUGIN_SECSKY
#import "$secsky"
#endif

void main() {

    vec3 look = screenToLook(vtex);
    vec3 color = sky(look);

    #ifdef PLUGIN_SECSKY
        color = pluginSecsky(look, color);
    #endif

    fragColor = vec4(color, 1.);
}