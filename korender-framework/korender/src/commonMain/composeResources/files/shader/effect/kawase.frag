#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniforms

uniform sampler2D colorInputTexture;

void main() {

    vec2 off = 1.0 / vec2(textureSize(colorInputTexture, 0));
    vec3 c1 = texture(colorInputTexture, vtex + off * vec2(-1.0, -1.0)).rgb;
    vec3 c2 = texture(colorInputTexture, vtex + off * vec2( 1.0, -1.0)).rgb;
    vec3 c3 = texture(colorInputTexture, vtex + off * vec2(-1.0,  1.0)).rgb;
    vec3 c4 = texture(colorInputTexture, vtex + off * vec2( 1.0,  1.0)).rgb;
    vec3 c = texture(colorInputTexture, vtex).rgb;

    vec3 result = (c1 + c2 + c3 + c4 + 4.0 * c) / 8.0;

    gl_FragColor = vec4(result, 1.0);
}