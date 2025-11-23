#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform sampler2D albedoGeometryTexture;
uniform sampler2D normalGeometryTexture;

out vec4 fragColor;
float depth;
vec3 color;

#uniforms

#ifdef SSR
    #import "!shader/deferred/composition-ssr.glsl"
#endif

#ifdef BLOOM
    #import "!shader/deferred/composition-bloom.glsl"
#endif

void main() {

    depth = texture(depthTexture, vtex).r;
    color = texture(colorTexture, vtex).rgb;

#ifdef SSR
    compositionSsr();
#endif

#ifdef BLOOM
    compositionBloom();
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}