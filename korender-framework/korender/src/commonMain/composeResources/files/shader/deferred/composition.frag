#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

#ifdef SSR
    uniform sampler2D ssrTexture;
    uniform sampler2D ssrDepth;
#endif

#ifdef BLOOM
    uniform sampler2D bloomTexture;
    uniform sampler2D bloomDepth;
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
    float ssrD = texture(ssrDepth, vtex).r;
    float ssrW  = 1.0 - smoothstep(depth - 0.23, depth + 0.23, ssrD);
    color += ssrW * texture(ssrTexture, vtex).rgb;
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