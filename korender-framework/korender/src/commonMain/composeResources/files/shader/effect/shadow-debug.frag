#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;
uniform sampler2D shadowTextures[12];

out vec4 fragColor;

float min = 0.25;

vec4 box(vec4 color, int index, sampler2D sampler) {
    vec2 uv = (vtex - vec2(min * index, 0.0)) / min;
    if (numShadows > index && uv.x > 0.0 && uv.x < 1.0 && uv.y > 0.0 && uv.y < 1.0) {
        if (uv.x < 0.01 || uv.x > 0.99 || uv.y < 0.01 || uv.y > 0.99)
            return vec4(0.7, 0.7, 0.7, 1.0);
        return texture(sampler, uv);
    }
    return color;
}


void main() {
    vec4 color = texture(colorInputTexture, vtex);
    color = box(color, 0, shadowTextures[0]);
    color = box(color, 1, shadowTextures[1]);
    color = box(color, 2, shadowTextures[2]);
    color = box(color, 3, shadowTextures[3]);
    fragColor = color;
}