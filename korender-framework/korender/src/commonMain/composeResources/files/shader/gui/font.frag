#import "!shader/lib/header.glsl"

in vec2 vtex;
#uniform vec4 color;
uniform sampler2D fontTexture;

#uniforms

out vec4 fragColor;

void main() {
    vec4 texel = texture(fontTexture, vtex) * color;
    fragColor = vec4(texel.rgb * texel.a, texel.a);
}