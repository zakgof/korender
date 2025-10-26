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

vec3 blurSameDepth(sampler2D tex, sampler2D depthTex, float depth, vec2 vtex, float radius, vec2 dir, float screen, float depthThreshold) {

    vec3 color = texture(tex, vtex).rgb;
    float w = 1.;
    vec2 step = dir / screen;

    for (float i = 1.0; i <= radius; i++) {
        float dim = exp(- i * i / (radius * radius));

        if (abs(texture(depthTex, vtex + step * i).r) - depth > depthThreshold) {
            color += texture(tex, vtex + step * i).rgb * dim;
            w += dim;
        }

        if (abs(texture(depthTex, vtex - step * i).r) - depth > depthThreshold) {
            color += texture(tex, vtex - step * i).rgb * dim;
            w += dim;
        }
    }
    return color / w;
}