#import "!shader/lib/noise.glsl"

float heightAt(vec2 uv) {

    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
        return -100.0;

    float samp = texture(heightTexture, uv).r;
    float base = samp * 800.0;

    float hs = clamp(samp - 0.1, 0.0, 1.0);

    float height =  -90.0 + base
        +    8.0 * fbm2(uv * 64.0) * hs
        +   16.0 * fbm2(uv * 16.0) * hs
        +  512.0 * fbm2(uv *  4.0) * hs * hs;

    return height;
}