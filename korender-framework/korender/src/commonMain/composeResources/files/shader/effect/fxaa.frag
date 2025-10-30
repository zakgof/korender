#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;

out vec4 fragColor;

#import "!shader/lib/fxaa.glsl"

void main() {
    vec3 color = fxaa(colorInputTexture, vtex, screenWidth, screenHeight);
    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(depthInputTexture, vtex).r;
}