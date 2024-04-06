#import "lib/header.glsl"

in vec2 vtex;
uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;
out vec4 fragColor;

void main() {
    vec4 c = texture(filterColorTexture, vtex);
    float bw = (c.r + c.g + c.b) * 0.3333;
    fragColor = vec4(bw, bw, bw, 1.);
    gl_FragDepth = texture(filterDepthTexture, vtex).r;
}