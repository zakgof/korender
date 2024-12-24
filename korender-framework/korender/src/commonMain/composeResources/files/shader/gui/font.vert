#import "shader/lib/header.glsl"

layout(location = 0) in vec2 tex;
layout(location = 1) in vec2 screen;

out vec2 vtex;

void main() {
    vtex = tex;
    gl_Position = vec4((screen * 2.0) - 1.0, -1.0, 1.0);
}