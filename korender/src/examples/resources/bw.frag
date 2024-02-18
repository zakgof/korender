#version 130

in vec2 vtex;
uniform sampler2D filterColorTexture;
out vec4 fragColor;

void main() {
    vec4 c = texture2D(filterColorTexture, vtex);
    float bw =( c.r + c.g + c.b) * 0.2;
    fragColor = vec4(bw, bw, bw, c.a);
}