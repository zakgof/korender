#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;
uniform sampler2D depthInputTexture;

#uniform vec3 fogColor;
#uniform float density;

#uniforms

out vec4 fragColor;

#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"

void main() {

    vec3 color = texture(colorInputTexture, vtex).rgb;
    float depth = texture(depthInputTexture, vtex).r;

    vec3 world = screenToWorldSpace(vtex, depth);
    float distance = length(world - cameraPos);

    float fogFactor = exp( -density * distance);

    color = mix(fogColor.rgb, color, fogFactor);

    fragColor = vec4(color, 1.0);
    gl_FragDepth = depth;
}