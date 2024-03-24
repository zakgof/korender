#import "header.glsl"

in vec2 vtex;
uniform sampler2D shadowTexture;
out vec4 fragColor;

void main() {
    vec4 c = texture(shadowTexture, vtex);
    fragColor = vec4(c);
}