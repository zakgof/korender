#import "lib/header.glsl"
#import "lib/sky.glsl"

in vec2 vtex;
uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;
uniform vec3 light;

out vec4 fragColor;

#import "$sky"

void main() {

    vec3 color = texture2D(filterColorTexture, vtex).rgb;
    float depth = texture2D(filterDepthTexture, vtex).r;

    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 w4 = inverse(projection * view) * vec4(csp, depth * 2.0 - 1.0, 1.0);
    vec3 world = w4.xyz / w4.w;
    vec3 look = normalize(world - cameraPos);

    vec3 surface = cameraPos - look * cameraPos.y / look.y;

    if (world.y < 0.4 * fbmTex(noiseTexture, surface.xz * 0.001 - 0.01 * time)) {

        vec3 normal = normalize(vec3(0.3 * fbmTex(noiseTexture, surface.xz * 0.004 - 0.01 * time), 1.0f, 0.3 * fbmTex(noiseTexture, surface.xz * 0.003 + 0.01 * time)));

        vec3 reflecteddir = reflect(look, normal);
        vec3 reflectedcolor = sky(reflecteddir).rgb;

        float R0 = 0.222;
        float t = 1. - clamp(dot(-look, normal), 0., 1.);
        float reflectance = R0 + (1. - R0) * t * t * t * t * t;

        float waterDepth = length(world - surface);
        vec3 ownColor = vec3(0.1, 0.2, 0.3);
        float ownRatio = clamp(waterDepth * 0.1, 0., 1.);

        ownColor = mix(color, ownColor, ownRatio);

        color = mix(ownColor, reflectedcolor, reflectance);
        // color = vec3(waterDepth * 0.0002, 0., 0.);

    }

    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(filterDepthTexture, vtex).r;
}