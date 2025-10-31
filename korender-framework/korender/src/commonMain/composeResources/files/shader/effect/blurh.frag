#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

#uniform float radius;

#uniforms

uniform sampler2D colorInputTexture;

out vec4 fragColor;

void main() {
    fragColor = vec4(blur(colorInputTexture, vtex, radius, vec2(1., 0.), screenWidth), 1.);
}