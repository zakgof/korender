#version 130

// Credits: Xavier Benech https://www.shadertoy.com/view/XsXSWS

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

uniform float startTime;
uniform float time;

out vec4 fragColor;

vec2 hash(vec2 p) {
    p = vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)));
    return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
}

float noise(in vec2 p) {
    const float K1 = 0.366025404; // (sqrt(3)-1)/2;
    const float K2 = 0.211324865; // (3-sqrt(3))/6;
    vec2 i = floor(p + (p.x + p.y) * K1);
    vec2 a = p - i + (i.x + i.y) * K2;
    vec2 o = (a.x > a.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec2 b = a - o + K2;
    vec2 c = a - 1.0 + 2.0 * K2;
    vec3 h = max(0.5 - vec3(dot(a, a), dot(b, b), dot(c, c)), 0.0);
    vec3 n = h * h * h * h * vec3(dot(a, hash(i + 0.0)), dot(b, hash(i + o)), dot(c, hash(i + 1.0)));
    return dot(n, vec3(70.0));
}

float fbm(vec2 uv) {
    float f;
    mat2 m = mat2(1.6, 1.2, -1.2, 1.6);
    f = 0.5000 * noise(uv); uv = m * uv;
    f += 0.2500 * noise(uv); uv = m * uv;
    f += 0.1250 * noise(uv); uv = m * uv;
    f += 0.0625 * noise(uv); uv = m * uv;
    f = 0.5 + 0.5 * f;
    return f;
}

float singleSmoker(vec2 uv, vec2 seed, float t) {
    vec2 q = (uv - 0.5) * 2.2 / t;
    float n = fbm(4.0 * q + seed * 0.8 * time);
    float c = 1. - 16. * pow(max(0., length(q) - n * .25), 1.2);
    float c1 = n * c * (1.5);
    c1 = clamp(c1, 0., 1.);
    return c1;
}

void main() {
    float dt = time - startTime;
    vec2 uv = vtex;
    float c1 = singleSmoker(uv, vec2(-1.0, 0.1), dt);
    float c2 = singleSmoker(uv, vec2(1.0, 0.0), dt);
    float c3 = singleSmoker(uv, vec2(-0.05, 0.9), dt);
    float c4 = singleSmoker(uv, vec2(0.0, -1.0), dt);
    float c = (c1 + c2 + c3 + c4) * 0.25;
    c = clamp(c, 0., 1.);
    float a = 1.0 / (dt*dt);
    if (c * a < 0.05)
       discard;
    fragColor = vec4(c, c, c, c * a);
}