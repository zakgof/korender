#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(colorTexture, vtex).rgb;

    float lumi = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float threshold = 0.9; // TODO threshold

    gl_FragColor = lumi > threshold ? vec4(color, 1.0) : vec4(0.0);
    gl_FragDepth = depth;
}