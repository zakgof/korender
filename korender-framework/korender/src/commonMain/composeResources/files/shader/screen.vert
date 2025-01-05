#import "!shader/lib/header.glsl"

layout(location = 2) in vec2 tex;
out vec2 vtex;

void main() {
    vtex = tex;
    gl_Position = vec4((tex * 2.0) - 1.0, -1.0, 1.0);
}