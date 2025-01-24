#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
in vec4 vsuper;

out vec4 fragColor;

void main() {
    // TODO: fully transparent pixels should not cast shadows, get color, sample textures and cut

    float d =  (vsuper.z / vsuper.w) * 0.5 + 0.5;

    float dx = dFdx(d);
    float dy = dFdy(d);

    float m1 = d * d;
    float m2 = d * d * d * d + 0.25 * (dx * dx + dy * dy);
    // TODO Depth is used anyway. Swizzling ?
    fragColor = vec4(m1, m2, 1.0, 1.0);
}