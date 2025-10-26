#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

#uniform float radius;

#uniforms

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

out vec4 fragColor;

void main() {
    float depth = texture(depthTexture, vtex).r;
    #ifdef SAME_DEPTH
        fragColor = vec4(blur(colorTexture, vtex, radius, vec2(1., 0.), screenHeight), 1.);
    #else
        fragColor = vec4(blurSameDepth(colorTexture, depthTexture, depth, vtex, radius, vec2(1., 0.), screenHeight), 1., 0.001);
    #endif
    gl_FragDepth = depth;
}