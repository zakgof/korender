#import "lib/noise.glsl"

uniform float time;
uniform float density;  // 0..2
uniform float marble1;  // 0..5
uniform float marble2;  // 0..5
uniform float scale;    // 0.1..10
uniform vec4 lightblue;
uniform vec4 darkblue;

vec3 sky(vec3 look) {

    vec2 uvorig = skydiskfromlook(look, 2.5) + time * 0.001;

    vec2 uv = uvorig - 0.01 * marble1 * fbm2(uvorig * 1.5) - 0.01 * marble2 * fbm2(uvorig * 4.5) ;
    float f = fbm2(uv * scale) - 0.5;
    f = density - 1.0 + 6.0*f + 6.0*f*f;

    float g = clamp(fbm2(uvorig * 0.6 - time * 0.003) + 0.4, 0., 1.);
    vec3 cloud = mix(vec3(1.0), vec3(0.9), clamp(f*g, 0., 3.));

    vec3 blue = mix(lightblue.rgb, darkblue.rgb, -look.y); // TODO light direction
    return mix(blue, cloud, clamp(f, 0., 1.));
}