#import "!shader/lib/header.glsl"

in vec2 vtex;

#uniforms

#uniform float threshold;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;
uniform sampler2D emissionGeometryTexture;

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 color = texture(colorTexture, vtex).rgb;
    vec3 emission = texture(emissionGeometryTexture, vtex).rgb;

    float lumi = dot(color, vec3(0.2126, 0.7152, 0.0722));

    vec3 result =  (lumi > threshold ? color : vec3(0.0)) + emission;
    gl_FragColor = vec4(result, 1.0);
    gl_FragDepth = depth;
}