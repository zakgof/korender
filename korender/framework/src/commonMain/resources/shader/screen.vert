#version 140

in vec2 tex;
out vec2 vtex;

void main() {
    vtex = tex;
    gl_Position = vec4((tex * 2.0) - 1.0, 0.99999, 1.0);
}