#version 130

in vec2 vtex;
uniform vec3 color;
uniform sampler2D fontTexture;

out vec4 fragColor;

void main() {
    fragColor = texture2D(fontTexture, vtex) * vec4(color, 1.0);
}