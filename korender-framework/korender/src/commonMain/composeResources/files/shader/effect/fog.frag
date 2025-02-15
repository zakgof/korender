#import "!shader/lib/header.glsl"
#import "!shader/lib/sky.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;

uniform vec3 fogColor;
uniform float density;

out vec4 fragColor;

void main() {

    vec3 color = texture(colorTexture, vtex).rgb;
    float depth = texture(depthTexture, vtex).r;

    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 w4 = inverse(projection * view) * vec4(csp, depth * 2.0 - 1.0, 1.0);
    vec3 world = w4.xyz / w4.w;

    float distance = length(world - cameraPos);

    float fogFactor = (depth >= 0.9999) ? 1.0 : exp( -density * distance);

    color = mix(fogColor.rgb, color, fogFactor);

    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(depthTexture, vtex).r;
}