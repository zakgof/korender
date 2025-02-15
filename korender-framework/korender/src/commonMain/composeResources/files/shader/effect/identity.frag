#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;

void main() {
    gl_FragColor = texture(colorTexture, vtex);
}