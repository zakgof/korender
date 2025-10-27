#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

#uniform float radius;

#uniforms

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

out vec4 fragColor;

void main() {
    fragColor = vec4(blur(colorTexture, vtex, radius, vec2(0., 1.), screenHeight), 1.);
    gl_FragDepth = texture(depthTexture, vtex).r;
}