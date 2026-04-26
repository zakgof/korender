#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

uniform sampler2D aoInputTexture;
uniform sampler2D depthGeometryTexture;
uniform sampler2D normalGeometryTexture;

#uniform vec2 direction;
#uniform int downsample;
#uniform float radius;

#uniforms

out vec4 fragColor;

void main() {
    fragColor = vec4(blur(aoInputTexture, vtex, radius, direction, screenWidth), 1.);
}
