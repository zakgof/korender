#import "!shader/lib/noise.glsl"

vec3 pluginTerrainCenter() {
    return vec3(0., -14., 0.);
}

int pluginTerrainTextureSize() {
    return 1024;
}

float pluginTerrainHeight(vec2 uv) {
    return  -1500.0
        + 100.0 * fbm(uv * 0.6)
        + 400.0 * fbm(uv * 0.2)
        + 281.0 * sin(uv.x * 2.3)
        + 1000.0 * sin(uv.x * 0.3)
        + 281.0 * cos(uv.y * 2.3)
        + 1000.0 * cos(uv.y * 0.3);
}