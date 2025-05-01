#import "!shader/lib/header.glsl"

in vec2 vtex;
in vec3 vpos;

uniform samplerCube envTexture0;
uniform samplerCube envDepthTexture0;
uniform float time;
uniform vec3 cameraPos;

out vec4 fragColor;


vec3 support(vec3 dir) {
    float depthSample = texture(envDepthTexture0, dir).r;
    vec3 center = vec3(0.0, 0.0, -20.0);
    return center + normalize(dir) * depthSample * 6.0;
}

void main() {

    vec3 look = normalize(vpos - cameraPos);

    vec3 v1 = -look;
    vec3 p1 = support(v1);
    vec3 r = cameraPos + look * dot (p1 - cameraPos, look);
    vec3 v0 = normalize(r - p1);
    vec3 p0 = support(v0);


    if (dot(v0, p0 - r) < 0.) {
        fragColor = vec4(0.);
        return;
    }

    fragColor = vec4(0., 1., 0., 1.);
    return;

    for (int i=0; i<5; i++) {
        vec3 v = normalize(v0 + v1);
        vec3 p = support(v);

        float looks = dot(v, look);
        float dotty = dot(v, p - r);

        if (looks >= 0.0 && dotty < 0.0) {
            fragColor = vec4(0.);
            return;
        }
        if (looks < 0.0 && dotty < 0.0) {
            r = r + look * (dotty / looks);
        }

        float d0 = dot(r - p0, r - p0);
        float d1 = dot(r - p1, r - p1);

        if (d0 > d1) {
            v0 = v;
            p0 = p;
        } else {
            v1 = v;
            p1 = p;
        }
    }

    fragColor = texture(envTexture0, p1);

}