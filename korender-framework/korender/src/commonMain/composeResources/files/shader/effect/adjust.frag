#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniform float brightness;   // -1..1, 0 is unity
#uniform float contrast;     // 0..2, 1 is unity
#uniform float saturation;   // 0..inf, 1 is unity

#uniforms

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
out vec4 fragColor;

void main() {
    vec4 color = texture(colorTexture, vtex);
    color.rgb += brightness;
    color.rgb = ((color.rgb - 0.5) * contrast) + 0.5;
    float lumi = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(vec3(lumi), color.rgb, saturation);
    fragColor = color;
    gl_FragDepth = texture(depthTexture, vtex).r;
}