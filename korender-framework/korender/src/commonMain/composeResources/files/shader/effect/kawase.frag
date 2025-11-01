#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniforms

#uniform float offset;
uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;

void main() {

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

    gl_FragColor = vec4((c1 + c2 + c3 + c4 + 4.0 * c) / 8.0, 1.0);
    gl_FragDepth = min(d, min(min(d1, d2), min(d3, d4)));
}