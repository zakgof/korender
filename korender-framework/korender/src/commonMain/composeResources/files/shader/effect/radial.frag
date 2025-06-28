#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl

in vec3 vcenter;
in vec3 vpos;
in vec2 vsize;

uniform samplerCube radiantTexture;
uniform samplerCube radiantNormalTexture;
uniform samplerCube colorTexture;
uniform samplerCube normalTexture;

out vec4 fragColor;

void main() {

    float radius = vsize.x * 0.5;
    vec3 look = normalize(vpos - cameraPos);

    vec3 p = cameraPos;

    float diff;
    vec3 n;
    vec3 dir;

    for (int i=0; i<8; i++) {
        vec3 ctop = p - vcenter;
        float cl = length(ctop);
        dir = ctop/cl;

        float radiant = radius * texture(radiantTexture, dir).r;
        n = texture(radiantNormalTexture, dir).rgb * 2. - 1.;

        diff = cl - radiant;
        float lambda = - diff * dot (n, dir) / dot(n, look);
        if (i > 0) {
            lambda = clamp(lambda, -0.05 * radius/float(i), 1.5 * radius /float(i));
        }

        p = p + look * lambda;
    }
    if (diff < 0.01 * radius) {
        vec3 normal = texture(normalTexture, dir).rgb * 2. - 1.;
        vec3 albedo = texture(colorTexture, dir).rgb;
        float light = 0.4 + 1.7 * clamp(dot(normal, normalize(-vec3(-1., 0., 1.))), 0., 1.);
        fragColor = vec4(light * albedo, 1.);

        vec4 vclip = projection * (view * vec4(p, 1.0));
        gl_FragDepth = 0.5 * vclip.z / vclip.w + 0.5;
    } else {
        discard;
    }
}