//////// normals coding //////////

vec3 decode_normal_2 (vec2 enc) {
    vec2 fenc = enc * 4.0 - vec2(2,2);
    float f = dot(fenc, fenc);
    float g = sqrt(1.0 - f*0.25);
    return vec3(fenc.x*g, 1.0 - f*0.5, fenc.y*g); 
}


vec3 decode_normal(vec2 enc) {
    vec2 xy = (enc - 0.5) * 2.0;
    return vec3(xy.x, sqrt(1.0 - dot(xy,xy)), xy.y); 
}

//////// noise and fbm //////////

float PHI_GR = 1.61803398874989484820459;   
mat2 fbm_rot = mat2(cos(0.5), sin(0.5), -sin(0.5), cos(0.5));
vec2 fbm_shift = vec2(1.0);

float gold_noise(in vec2 xy, in float seed){
    return fract(tan(distance(xy*PHI_GR, xy)*seed)*xy.x);
}

float fbm1(in vec2 uv) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; ++i) {
        v += a * gold_noise(uv, 0.001);
        uv = fbm_rot * uv * 2.0 + fbm_shift;
        a *= 0.5;
    }
    return v;
}