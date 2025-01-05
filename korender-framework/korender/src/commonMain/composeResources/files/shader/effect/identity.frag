#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D filterColorTexture;

void main() {
    gl_FragColor = texture(filterColorTexture, vtex);
}