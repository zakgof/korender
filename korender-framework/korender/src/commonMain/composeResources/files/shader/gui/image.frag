#import "!shader/lib/header.glsl"

in vec2 vtex;

out vec4 fragColor;

uniform sampler2D imageTexture;

void main() {
    vec4 texel = texture(imageTexture, vtex);
    fragColor = vec4(texel.rgb * texel.a, texel.a);
}