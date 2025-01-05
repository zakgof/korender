#import "!shader/lib/header.glsl"
#import "!shader/lib/sky.glsl"

in vec2 vtex;

uniform vec4 waterColor;
uniform float transparency;
uniform float waveScale;


uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;
uniform vec3 lightDir;
uniform vec4 lightColor;

out vec4 fragColor;

#import "$sky"

void main() {

    vec3 color = texture(filterColorTexture, vtex).rgb;
    float depth = texture(filterDepthTexture, vtex).r;

    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 w4 = inverse(projection * view) * vec4(csp, depth * 2.0 - 1.0, 1.0);
    vec3 world = w4.xyz / w4.w;
    vec3 look = normalize(world - cameraPos);

    vec3 surface = cameraPos - look * cameraPos.y / look.y;

    float fbmA = fbm2(surface.xz * waveScale - 0.03 * time) - 0.5;
    if (world.y < 0.4 * fbmA) {

        vec3 normal = normalize(vec3(
            0.3 * fbmA,
            1.0f,
            0.3 * (fbm2(surface.xz * 0.03 + 0.04 * time) - 0.5)
        ));

        vec3 reflecteddir = reflect(look, normal);
        vec3 reflectedcolor = sky(reflecteddir).rgb;

        float R0 = 0.0222;
        float t = 1. - clamp(dot(-look, normal), 0., 1.);
        float reflectance = R0 + (1. - R0) * t * t * t * t * t;

        float waterDepth = length(world - surface);
        vec3 ownColor = waterColor.rgb;
        float ownRatio = clamp(waterDepth * transparency, 0., 1.);

        ownColor = mix(color, ownColor, ownRatio);

        color = mix(ownColor, reflectedcolor, reflectance * 0.7);
    }

    fragColor = vec4(color, 1.0);
    gl_FragDepth = texture(filterDepthTexture, vtex).r;
}