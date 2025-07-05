#import "!shader/lib/header.glsl"

layout(location = 2) in vec2 tex;
layout(location = 11) in vec4 insttexrect;
layout(location = 12) in vec4 instscreenrect;

out vec2 vtex;

void main() {
    vtex = insttexrect.xy + tex * insttexrect.zw;
    vec2 screen = instscreenrect.xy + tex * instscreenrect.zw;
    gl_Position = vec4((screen * 2.0) - 1.0, -1.0, 1.0);
}