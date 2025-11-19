#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

#uniforms

#ifdef SSR
    #import "!shader/deferred/composition-ssr.glsl"
#endif

#ifdef BLOOM
    #import "!shader/deferred/composition-bloom.glsl"
#endif

out vec4 fragColor;

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(colorTexture, vtex).rgb;

#ifdef SSR
    color = compositionSsr(color, depth, vtex);
#endif

#ifdef BLOOM
    color = compositionBloom(color, depth, vtex);
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}