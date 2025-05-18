#import "!shader/lib/header.glsl"

in vec3 vcenter;
in vec3 vpos;

uniform samplerCube radiantTexture;
uniform samplerCube radiantNormalTexture;
uniform samplerCube colorTexture;
uniform samplerCube normalTexture;
uniform vec3 cameraPos;

out vec4 fragColor;


void main() {

    vec3 look = normalize(vpos - cameraPos);

    vec3 p = cameraPos;

    float diff;
    vec3 n;
    vec3 dir;

    for (int i=0; i<8; i++) {
        vec3 ctop = p - vcenter;
        float cl = length(ctop);
        dir = ctop/cl;

        float radiant = 5.0 * texture(radiantTexture, dir).r;
        n = texture(radiantNormalTexture, dir).rgb * 2. - 1.;

        diff = cl - radiant;
        float lambda = - diff * dot (n, dir) / dot(n, look);
        if (i > 0) {
            lambda = clamp(lambda, -0.05 * 20.0 /i, 1.5 * 20.0 /i);
        }

        p = p + look * lambda;
    }
    fragColor = vec4(0.);
    if (diff < 0.1) {
        vec3 normal = texture(normalTexture, dir).rgb * 2. - 1.;
        vec3 albedo = texture(colorTexture, dir).rgb;
        float light = 0.4 + 1.7 * clamp(dot(normal, normalize(vec3(-1., 0., 1.))), 0., 1.);
        fragColor = vec4(light * albedo, 1.);
    }
}