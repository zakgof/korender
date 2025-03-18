#import "!shader/lib/noise.glsl"

float heightAt(vec2 uv) {

    float base =  100.0 * fbm2(uv)
        + 150.0 * cos(uv.x * 7.0)
        + 180.0 * sin(uv.x * 2.3)
        + 400.0 * sin(uv.x * 0.3)
        + 150.0 * cos(uv.y * 7.0)
        + 180.0 * sin(uv.y * 2.3)
        + 400.0 * cos(uv.y * 0.3);

    return base;
}