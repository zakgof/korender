#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D finalColorTexture;
uniform sampler2D depthTexture;

#ifdef SSR
uniform sampler2D ssrTexture;
    #ifdef SSR_FXAA
        #uniform float ssrWidth;
        #uniform float ssrHeight;
    #endif
#endif

#ifdef BLOOM
uniform sampler2D bloomTexture;
#endif

#uniforms

out vec4 fragColor;

#ifdef SSR_FXAA
    #import "!shader/lib/fxaa.glsl"
#endif

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(finalColorTexture, vtex).rgb;

#ifdef SSR
    #ifdef SSR_FXAA
        color += fxaa(ssrTexture, vtex, ssrWidth, ssrHeight);
    #else
        color += texture(ssrTexture, vtex).rgb;
    #endif
#endif

#ifdef BLOOM
    color += texture(bloomTexture, vtex).rgb;
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}