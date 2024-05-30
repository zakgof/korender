uniform float time;
uniform float screenWidth;
uniform float screenHeight;

// https://www.shadertoy.com/view/XlfGRj
#define iterations 17
#define formuparam 0.53
#define volsteps 20
#define stepsize 0.1
#define zoom   0.800
#define tile   0.850
#define speed  0.001
#define brightness 0.0015
#define darkmatter 0.300
#define distfading 0.730
#define saturation 0.850

vec3 sky(vec3 look) {

    vec2 uv = skydiskfromlook(look, 1.5);

    vec3 dir = vec3(uv * zoom, 1.);

    float chrono = time * speed + .25;

    vec3 from = vec3(1., .5, 0.5);
    from += vec3(chrono * 2., chrono, -2.);

    //volumetric rendering
    float s = 0.1, fade = 1.;
    vec3 v = vec3(0.);
    for (int r = 0; r < volsteps; r++) {
        vec3 p = from + s * dir * .5;
        p = abs(vec3(tile) - mod(p, vec3(tile * 2.)));// tiling fold
        float pa, a = pa = 0.;
        for (int i = 0; i< iterations; i++) {
            p = abs(p) / dot(p, p) - formuparam;// the magic formula
            a+=abs(length(p) - pa);// absolute sum of average change
            pa =length(p);
        }
        float dm = max(0., darkmatter - a* a * .001);//dark matter
        a*=a *a;// add contrast
        if (r > 6) fade*=1. - dm;// dark matter, don't render near
        //v+=vec3(dm,dm*.5,0.);
        v+=fade;
        v+=vec3(s, s * s, s *s * s * s) * a * brightness* fade;// coloring based on distance
        fade*=distfading;// distance fading
        s+=stepsize;
    }
    v = mix(vec3(length(v)), v, saturation);//color adjust
    return vec3(v * .01);
}