#import "!shader/lib/noise.glsl"

uniform sampler2D noiseTexture;

#uniform float colorness;
#uniform float density;
#uniform float speed;
#uniform float size;

float noi(vec2 uv) {
    return fract(texture(noiseTexture, uv).r * 5.0) * 0.8 + 0.1;
}

vec3 sky(vec3 look, float bias) {

    vec2 uve = skydiskfromlook(look, 1.5);

    vec3 color = vec3(0.0);

    for (int i=0; i<4; i++) {

        float b = float(i) * 0.2;
        vec2 uv = mat2(cos(b), -sin(b), sin(b), cos(b)) * uve + time * speed * 0.001;

        float a = density * (1.0 + float(i) * 0.25);
        vec2 cell = floor(uv * a);
        vec2 offs = uv*a - cell;

        vec3 shift = vec3(
        noi(cell * 1.1234),
        noi(cell * 1.523 + vec2(0.37)),
        noi(cell * 1.923 + vec2(0.41))
        );
        vec2 center = cell + shift.xy;
        vec2 tocenter = uv*a - center;
        float dist = dot(tocenter, tocenter);
        float sz = shift.x * shift.y * shift.z * density * size * 0.000002;
        color += (vec3(1.0 - colorness) + shift * colorness) * min(sz/dist, 1.0);
    }
    return color;
}