#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D filterColorTexture;

uniform int numShadows;
const int MAX_SHADOWS = 12;
uniform sampler2D shadowTextures[MAX_SHADOWS];

out vec4 fragColor;

void main() {
    vec4 color = texture(filterColorTexture, vtex);
    float min = 0.2;
    if (numShadows > 0 && vtex.x < min && vtex.y < min) {
        color = texture(shadowTextures[0], vtex / min);
    }
    if (numShadows > 1 && vtex.x > min && vtex.x < min * 2. && vtex.y < min) {
        float shadow = texture(shadowTextures[1], (vtex - vec2(min, 0.0)) / min).r;
        color = vec4(0., shadow, 0., 1.);
    }
    fragColor = color;
}