vec3 blur(sampler2D tex, vec2 vtex, float radius, vec2 dir, float screen) {

    vec3 color = texture(tex, vtex).rgb;
    float w = 1.;
    vec2 step = dir / screen;

    for (float i = 1.0; i <= radius; i++) {
        float dim = exp(- i * i / (radius * radius));
        w += dim * 2.0;
        color += texture(tex, vtex + step * i).rgb * dim;
        color += texture(tex, vtex - step * i).rgb * dim;
    }
    return color / w;
}

float minDepthBlur(sampler2D tex, vec2 vtex, float radius, vec2 dir, float screen) {

    float depth = texture(tex, vtex).r;
    vec2 step = dir / screen;

    for (float i = 1.0; i <= radius; i++) {
        depth = min(depth, texture(tex, vtex + step * i).r);
        depth = min(depth, texture(tex, vtex - step * i).r);
    }
    return depth;
}