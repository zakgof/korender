#import "!shader/lib/header.glsl"

layout(location = 2) in vec2 tex;
layout(location = 5) in vec2 screen;

out vec2 vtex;

void main() {
    vtex = tex;
    gl_Position = vec4((screen * 2.0) - 1.0, -1.0, 1.0);
}