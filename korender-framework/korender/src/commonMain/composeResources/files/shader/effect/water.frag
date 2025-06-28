#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

#uniform vec3 waterColor;
#uniform float transparency;
#uniform float waveScale;
#uniform float waveMagnitude;

#uniforms

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

out vec4 fragColor;

#import "!shader/lib/space.glsl"
#import "!shader/lib/sky.glsl"
#import "$sky"

#ifdef PLUGIN_SECSKY
    #import "$secsky"
#endif

void main() {

    vec3 color = texture(colorTexture, vtex).rgb;
    float depth = texture(depthTexture, vtex).r;

    vec3 look = screenToLook(vtex);
    vec3 world = screenToWorldSpace(vtex, depth);
    if (depth > 0.9999) {
        world = cameraPos + look * 10000000.0;
    }

    vec3 surface = cameraPos - look * cameraPos.y / look.y;

    float fbmA = fbm(surface.xz / waveScale - 0.03 * time) - 0.5;
    if (world.y < waveMagnitude * fbmA) {

        vec3 normal = normalize(vec3(
            waveMagnitude * fbmA,
            1.0f,
            waveMagnitude * (fbm((msw * surface.xz) / waveScale  + 0.04 * time) - 0.5)
        ));

        vec3 reflecteddir = reflect(look, normal);
        vec3 reflectedcolor = sky(reflecteddir, 0.).rgb;

        #ifdef PLUGIN_SECSKY
            reflectedcolor = pluginSecsky(reflecteddir, reflectedcolor);
        #endif

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
    gl_FragDepth = texture(depthTexture, vtex).r;
}