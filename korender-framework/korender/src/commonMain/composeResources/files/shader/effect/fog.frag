#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

#uniform vec3 fogColor;
#uniform float density;

#uniforms

out vec4 fragColor;

#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"

void main() {

    vec3 color = texture(colorTexture, vtex).rgb;
    float depth = texture(depthTexture, vtex).r;

    vec3 world = screenToWorldSpace(vtex, depth);

    float distance = length(world - cameraPos);

    float fogFactor = exp( -density * distance);

    color = mix(fogColor.rgb, color, fogFactor);

    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(depthTexture, vtex).r;
}