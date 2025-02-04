#import "!shader/lib/noise.glsl"

uniform float time;
uniform float thickness;
uniform float density;
uniform float scale;
uniform float rippleamount;
uniform float ripplescale;
uniform vec3 lightblue;
uniform vec3 darkblue;

vec3 sky(vec3 look) {

    vec2 uv = skydiskfromlook(look, 4.5) + time * 0.001;

    float f = thickness * ((fbm2(uv * scale) - 0.5) +
              rippleamount * (fbm2(uv * scale * ripplescale) - 0.5)) +
              density - 3.;

    float g = clamp(fbm2(uv * 0.6 - time * 0.003) + 0.4, 0., 1.);
    vec3 cloud = mix(vec3(1.0), vec3(0.9), clamp(f*g, 0., 3.));

    vec3 blue = mix(lightblue, darkblue, -look.y); // TODO light direction
    return mix(blue, cloud, clamp(f, 0., 1.));
}