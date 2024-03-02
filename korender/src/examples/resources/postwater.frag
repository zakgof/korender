#version 140

#import "cloudsky.glsl"

in vec2 vtex;
uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;
uniform vec3 light;
uniform float time;

out vec4 fragColor;

void main() {

    vec3 color = texture2D(filterColorTexture, vtex).rgb;
    float depth = texture2D(filterDepthTexture, vtex).r;

    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 w4 = inverse(projection * view) * vec4(csp, depth * 2.0 - 1.0, 1.0);
    vec3 world = w4.xyz / w4.w;
    vec3 look = normalize(world - cameraPos);


    if (world.y < 1.0 + fbm(world.xz * 0.1 + time * 0.2) * 0.4) {

        float tt = - cameraPos.y / look.y;
        world = cameraPos + look * tt;
        color = vec3(0.3, 0.3, 0.3);

        vec3 normal = normalize(vec3(0.3 * fbm(world.xz * 0.05 - time), 1.0f, 0.3 * fbm(world.xz * 0.07 + time)));

        vec3 reflecteddir = reflect(look, normal);
        vec3 reflectedcolor = cloudsky(reflecteddir, time).rgb;

        float R0 = 0.0222;
        float t = 1.0 - clamp(dot(-look, normal), 0.0, 1.0);
        float reflectance = R0 + (1. - R0) * t * t * t * t * t;

        float waterDepth = (world.y + 0.1);
        vec3 ownColor = vec3(0.1, 0.2, 0.3);
        float ownRatio = clamp(waterDepth * 0.2, 0., 1.);

        ownColor = mix(color, ownColor, ownRatio);

        color = mix(ownColor, reflectedcolor, reflectance);
    }
    fragColor = vec4(color, 1.0);
}