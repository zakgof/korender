#import "!shader/lib/header.glsl"

in vec3 vcenter;
in vec3 vpos;

uniform samplerCube radiantTexture;
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

        vec4 smpl = texture(radiantTexture, dir);
        float radiant = 1.5 * smpl.b;

        float PI = 3.1415926;
        float theta = (smpl.r - 0.5) * 2.0 * PI;
        float phi = smpl.g * PI;
        n = vec3(sin(phi) * cos(theta), sin(phi) * sin(theta), cos(phi));

        diff = cl - radiant;
        float lambda = - diff * dot (n, dir) / dot(n, look);
        if (i > 0) {
            lambda = clamp(lambda, -0.05/i, 1.5/i);
        }

        p = p + look * lambda;
    }
    fragColor = vec4(0.);
    if (diff < 0.008) {
        vec3 normal = texture(normalTexture, dir).rgb * 2. - 1.;
        vec3 albedo = texture(colorTexture, dir).rgb;
        float light = 0.3 + 0.7 * clamp(dot(normal, normalize(vec3(1., 0., 1.))), 0., 1.);
        fragColor = vec4(light * albedo, 1.);
    }
}