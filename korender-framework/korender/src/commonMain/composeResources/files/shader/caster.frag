#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

out vec4 fragColor;

void main() {
    // TODO: fully transparent pixels should not cast shadows, get color, sample textures and cut

#ifdef VSM_SHADOW
    float d = gl_FragCoord.z;
    float m1 = d * d;
    float dx = dFdx(m1);
    float dy = dFdy(m1);
    float m2 = m1 * m1 + 0.25 * (dx * dx + dy * dy);
    // TODO Depth is used anyway. Swizzling ?
    fragColor = vec4(m1, m2, 1.0, 1.0);
#else

#endif

}