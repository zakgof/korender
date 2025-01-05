#import "!shader/lib/noise.glsl"

// https://www.shadertoy.com/view/4tdSWr

const float cloudscale = 8.0;
const float speed = 0.01;
const float clouddark = 0.5;
const float cloudlight = 0.3;
const float cloudcover = 0.2;
const float cloudalpha = 8.0;
const float skytint = 0.5;
const vec3 skycolour1 = vec3(0.2, 0.4, 0.6);
const vec3 skycolour2 = vec3(0.4, 0.7, 1.0);

uniform float time;

vec3 sky(vec3 look) {

    vec2 UV = skydiskfromlook(look, 2.5);

    float chrono = time * speed;
    float q = fbm(UV * cloudscale * 0.5);

    //ridged noise shape
    float r = 0.0;
    vec2 uv = UV * cloudscale;
    uv -= q - chrono;
    float weight = 0.8;
    for (int i = 0; i < 8; i++) {
        r += abs(weight * noise(uv));
        uv = msw * uv + chrono;
        weight *= 0.7;
    }

    //noise shape
    float f = 0.0;
    uv = UV * cloudscale;
    uv -= q - chrono;
    weight = 0.7;
    for (int i = 0; i < 8; i++) {
        f += weight * noise(uv);
        uv = msw * uv + chrono;
        weight *= 0.6;
    }

    f *= r + f;

    //noise colour
    float c = 0.0;
    chrono = time * speed * 2.0;
    uv = UV * cloudscale * 2.0;
    uv -= q - chrono;
    weight = 0.4;
    for (int i = 0; i < 7; i++) {
        c += weight * noise(uv);
        uv = msw * uv + chrono;
        weight *= 0.6;
    }

    //noise ridge colour
    float c1 = 0.0;
    chrono = time * speed * 3.0;
    uv = UV * cloudscale * 3.0;
    uv -= q - chrono;
    weight = 0.4;
    for (int i = 0; i < 7; i++) {
        c1 += abs(weight * noise(uv));
        uv = msw * uv + chrono;
        weight *= 0.6;
    }

    c += c1;

    vec3 skycolour = mix(skycolour2, skycolour1, -look.y);
    vec3 cloudcolour = vec3(1.1, 1.1, 0.9) * clamp((clouddark + cloudlight * c), 0.0, 1.0);

    f = cloudcover + cloudalpha * f * r;

    return mix(skycolour, clamp(skytint * skycolour + cloudcolour, 0.0, 1.0), clamp(f + c, 0.0, 1.0));
}