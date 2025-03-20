#import "!shader/lib/noise.glsl"

float heightAt(vec2 uv) {

    float base =  -1500.0
        + 100.0 * fbm2(uv * 0.6)
        + 400.0 * fbm2(uv * 0.2)
        + 281.0 * sin(uv.x * 2.3)
        + 1000.0 * sin(uv.x * 0.3)
        + 281.0 * cos(uv.y * 2.3)
        + 1000.0 * cos(uv.y * 0.3);

    return base;
}