#import "!shader/lib/header.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

out vec4 fragColor;

void main() {
    // TODO: fully transparent pixels should not cast shadows, get color, sample textures and cut
    float d = gl_FragCoord.z;
    float dx = dFdx(d);
    float dy = dFdy(d);
    float m2 = d * d + min(0.25 * (dx * dx + dy * dy), 0.25);
    // TODO Depth is used anyway. Swizzling ?
    fragColor = vec4(d, m2, 0.0, 1.0);
}