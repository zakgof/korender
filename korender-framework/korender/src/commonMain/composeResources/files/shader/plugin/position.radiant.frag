uniform samplerCube radiantTexture;
uniform samplerCube radiantNormalTexture;

in vec2 vsize;
in vec3 vcenter;

vec3 radiantDir;

vec3 pluginPosition() {

    float radius = vsize.x * 0.5;
    vec3 look = normalize(vpos - cameraPos);

    vec3 p = cameraPos;

    float diff;
    vec3 n;

    for (int i=0; i<4; i++) {
        vec3 ctop = p - vcenter;
        float cl = length(ctop);
        radiantDir = ctop/cl;

        float radiant = radius * texture(radiantTexture, radiantDir).r;
        n = texture(radiantNormalTexture, radiantDir).rgb * 2. - 1.;

        diff = cl - radiant;
        float lambda = - diff * dot (n, radiantDir) / dot(n, look);
//        if (i > 0) {
//            lambda = clamp(lambda, -0.05 * radius/float(i), 1.5 * radius /float(i));
//        }

        p = p + look * lambda;
    }
    if (diff > 0.01 * radius) {
        discard;
    }
    return p;
}