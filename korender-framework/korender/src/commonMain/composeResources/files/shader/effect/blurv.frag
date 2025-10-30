#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

#uniform float radius;

#uniforms

uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;

out vec4 fragColor;

void main() {
    fragColor = vec4(blur(colorInputTexture, vtex, radius, vec2(0., 1.), screenHeight), 1.);
    gl_FragDepth = texture(depthInputTexture, vtex).r;
}