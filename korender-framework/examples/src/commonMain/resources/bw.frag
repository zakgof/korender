#import "header.glsl"

in vec2 vtex;
uniform sampler2D filterColorTexture;
out vec4 fragColor;

void main() {
    vec4 c = texture(filterColorTexture, vtex);
    float bw = (c.r + c.g + c.b) * 0.3333;
    fragColor = vec4(bw, bw, bw, 1.);
}