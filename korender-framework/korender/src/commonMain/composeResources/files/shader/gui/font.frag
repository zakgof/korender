#import "!shader/lib/header.glsl"

in vec2 vtex;
uniform vec4 color;
uniform sampler2D fontTexture;

out vec4 fragColor;

void main() {
    fragColor = texture(fontTexture, vtex) * color;
}