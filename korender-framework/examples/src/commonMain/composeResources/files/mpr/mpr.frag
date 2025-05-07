#import "!shader/lib/header.glsl"

in vec2 vtex;
in vec3 vcenter;
in vec3 vpos;

uniform samplerCube envTexture0;
uniform float time;
uniform vec3 cameraPos;

out vec4 fragColor;


void main() {

    vec3 look = normalize(vpos - cameraPos);

    vec3 p = cameraPos;

    float diff;
    vec3 n;

    for (int i=0; i<8; i++) {
        vec3 ctop = p - vcenter;
        float cl = length(ctop);
        vec3 dir = ctop/cl;

        vec4 smpl = texture(envTexture0, dir);
        float radiant = 1.0 * smpl.b;
        // n = normalize(smpl.rgb * 2.0 - 1.0);

        n = vec3(smpl.rg * 2.0 - 1.0, 0.0);
        n.z = 1.0 - abs(n.x) - abs(n.y);
        if (n.z < 0.0) {
            n.xy = (1.0 - abs(n.yx)) * sign(n.xy);
        }
        n = normalize(n);

        // radiant = 1.0;
        // n = normalize(dir);

        diff = cl - radiant;
        float lambda = - diff * dot (n, dir) / dot(n, look);
        if (i > 0) {
            lambda = clamp(lambda, -0.05/i, 1.5/i);
        }

        p = p + look * lambda;
    }
    if (diff < 0.008) {
        float c = 0.1 + 0.9 * clamp(dot(n, normalize(vec3(1., 0., -1.))), 0., 1.);
        fragColor = vec4(0., c, 0., 1.);
    } else {
        fragColor = vec4(0.);
    }

}