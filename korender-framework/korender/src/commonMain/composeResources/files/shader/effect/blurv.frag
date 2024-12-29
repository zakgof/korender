#import "!shader/lib/header.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

uniform float radius;

uniform float screenHeight;
uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;

out vec4 fragColor;

void main() {
    fragColor = vec4(blur(filterColorTexture, vtex, radius, vec2(0., 1.), screenHeight), 1.);
    gl_FragDepth = texture(filterDepthTexture, vtex).r;
}