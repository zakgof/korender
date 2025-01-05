#import "!shader/lib/header.glsl"

in vec2 vtex;

out vec4 fragColor;

uniform sampler2D imageTexture;

void main() {
    fragColor = texture(imageTexture, vtex);
}