#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
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
    uniform sampler2D bloomDepth;
    uniform sampler2D downsample0;
    #uniform float bloomAmount;
#endif

#uniforms

out vec4 fragColor;

#ifdef SSR_FXAA
    #import "!shader/lib/fxaa.glsl"
#endif

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(colorTexture, vtex).rgb;

#ifdef SSR
    #ifdef SSR_FXAA
        color += fxaa(ssrTexture, vtex, ssrWidth, ssrHeight);
    #else
        color += texture(ssrTexture, vtex).rgb;
    #endif
#endif

#ifdef BLOOM
    vec4 bloomSample = texture(bloomTexture, vtex);
    float bDepth = texture(bloomDepth, vtex).r;
    float depthRatio = smoothstep(depth + 0.1, depth - 0.1, bDepth);
    color += bloomSample.rgb * depthRatio * bloomAmount;
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}