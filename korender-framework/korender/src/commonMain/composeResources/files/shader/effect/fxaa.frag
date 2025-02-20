#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform float screenWidth;
uniform float screenHeight;

out vec4 fragColor;

#import "!shader/lib/fxaa.glsl"

void main() {
    vec3 color = fxaa(colorTexture, vtex, screenWidth, screenHeight);
    fragColor = vec4(color, 1.0);
}