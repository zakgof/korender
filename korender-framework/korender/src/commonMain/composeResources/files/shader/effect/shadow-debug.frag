#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D shadowTextures[12];

out vec4 fragColor;

void main() {

    vec4 color = texture(colorTexture, vtex);

    float min = 0.4;

    if (numShadows > 0 && vtex.x < min && vtex.y < min) {
        color = texture(shadowTextures[0], vtex / min);
    }
    if (numShadows > 1 && vtex.x > min * 1. && vtex.x < min * 2. && vtex.y < min) {
        color = texture(shadowTextures[1], (vtex - vec2(min, 0.0)) / min);
    }
    if (numShadows > 2 && vtex.x > min * 2. && vtex.x < min * 3. && vtex.y < min) {
        color = texture(shadowTextures[2], (vtex - vec2(2. * min, 0.0)) / min);
    }

    fragColor = color;
}