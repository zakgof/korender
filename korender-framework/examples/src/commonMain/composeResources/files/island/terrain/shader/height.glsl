#import "!shader/lib/noise.glsl"

uniform sampler2D heightTexture;

vec3 pluginTerrainCenter() {
    return vec3(0.);
}

int pluginTerrainTextureSize() {
    return 512;
}

float pluginTerrainHeight(vec2 uv) {

    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
        return - 256.0 * 16.0 * 0.1;

    vec4 samp = texture(heightTexture, uv);

    float base = (samp.g * 255.0 + samp.r) * 16.0 - 256.0 * 16.0 * 0.1;


    return base + 0.3 * fbm(uv * 256.0);
}