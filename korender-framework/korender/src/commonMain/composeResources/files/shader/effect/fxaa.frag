#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

out vec4 fragColor;

#import "!shader/lib/fxaa.glsl"

void main() {
    vec3 color = fxaa(colorTexture, vtex, screenWidth, screenHeight);
    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(depthTexture, vtex).r;
}