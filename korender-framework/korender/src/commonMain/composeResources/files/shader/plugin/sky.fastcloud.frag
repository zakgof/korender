#import "!shader/lib/noisebias.glsl"

#uniform float thickness;
#uniform float density;
#uniform float scale;
#uniform float rippleamount;
#uniform float ripplescale;
#uniform vec3 zenithcolor;
#uniform vec3 horizoncolor;
#uniform float cloudlight;
#uniform float clouddark;

vec3 sky(vec3 look, float bias) {

    vec2 uv = skydiskfromlook(look, 4.5) + time * 0.001;

    float f = thickness * ((fbm2(uv * scale, bias) - 0.5) +
              rippleamount * (fbm2(uv * scale * ripplescale, bias) - 0.5)) +
              density - 3.;

    vec3 blue = mix(zenithcolor, horizoncolor, pow(1. - look.y, 6.)); // TODO light direction

    float g = clamp(fbm2(uv * 0.6 - time * 0.003, bias) + 0.4, 0., 1.);
    float cloud = mix(cloudlight, clouddark, clamp(f * g * 0.3, 0., 1.));

    return mix(blue, vec3(cloud), clamp(f, 0., 1.));
}