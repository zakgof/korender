#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;

void main() {
    gl_FragColor = texture(colorInputTexture, vtex);
    gl_FragDepth = texture(depthInputTexture, vtex).r;
}