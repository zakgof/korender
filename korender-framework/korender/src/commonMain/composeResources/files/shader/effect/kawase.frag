#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniforms

#uniform float offset;
uniform sampler2D colorInputTexture;
#ifdef UPSAMPLE
uniform sampler2D highResTexture;
#uniform float highResolutionRatio;
#endif

uniform sampler2D depthInputTexture;

out vec4 fragColor;

void main() {

    #ifdef UPSAMPLE
        float centerWeight = 4.0;
    #else
        float centerWeight = 0.0;
    #endif

    vec2 off = offset / vec2(textureSize(colorInputTexture, 0));
    vec2 t1 = vtex + off * vec2(-1.0, -1.0);
    vec2 t2 = vtex + off * vec2( 1.0, -1.0);
    vec2 t3 = vtex + off * vec2(-1.0,  1.0);
    vec2 t4 = vtex + off * vec2( 1.0,  1.0);
    vec3 c1 = texture(colorInputTexture, t1).rgb;
    vec3 c2 = texture(colorInputTexture, t2).rgb;
    vec3 c3 = texture(colorInputTexture, t3).rgb;
    vec3 c4 = texture(colorInputTexture, t4).rgb;
    vec3 c  = texture(colorInputTexture, vtex).rgb;

    float d1 = texture(depthInputTexture, t1).r;
    float d2 = texture(depthInputTexture, t2).r;
    float d3 = texture(depthInputTexture, t3).r;
    float d4 = texture(depthInputTexture, t4).r;
    float d  = texture(depthInputTexture, vtex).r;

    vec3 color = (c1 + c2 + c3 + c4 + centerWeight * c) / (centerWeight + 4.0);

    #ifdef UPSAMPLE
        vec3 highColor = texture(highResTexture, vtex).rgb;
        color += highColor * highResolutionRatio;
    #endif

    fragColor = vec4(color, 1.0);
    gl_FragDepth = min(d, min(min(d1, d2), min(d3, d4)));
}