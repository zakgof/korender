#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

layout(location = 2) in vec2 tex;
out vec2 vtex;

#uniform vec2 pos;
#uniform vec2 size;
#uniforms

void main() {
    vtex = vec2(tex.x, 1.0 - tex.y);
    vec2 quad = pos + tex * size;
    gl_Position = vec4((quad * 2.0) - 1.0, -1.0, 1.0);
}