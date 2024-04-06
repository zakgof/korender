#import "lib/header.glsl"

in vec2 vtex;
uniform vec3 color;
uniform sampler2D fontTexture;

out vec4 fragColor;

void main() {
    fragColor = texture(fontTexture, vtex) * vec4(color.rgb, 1.0);
}