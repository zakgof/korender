#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D finalColorTexture;
uniform sampler2D depthTexture;

#ifdef SSR
uniform sampler2D ssrTexture;
#endif

#ifdef BLOOM
uniform sampler2D bloomTexture;
#endif

out vec4 fragColor;

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(finalColorTexture, vtex).rgb;

#ifdef SSR
    color += texture(ssrTexture, vtex).rgb;
#endif

#ifdef BLOOM
    color += texture(bloomTexture, vtex).rgb;
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}