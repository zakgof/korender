#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/blur.glsl"

in vec2 vtex;

uniform sampler2D aoInputTexture;
uniform sampler2D depthGeometryTexture;
uniform sampler2D normalGeometryTexture;

#uniform vec2 direction;
#uniform int downsample;
#uniform float radius;

#uniforms

out vec4 fragColor;

vec2 blurPoint(vec2 tex, float dim, float centerDepth, vec3 centerNormal) {
    float depth = textureLod(depthGeometryTexture, tex, 0.0).r;
    vec3 normal = normalize(textureLod(normalGeometryTexture, tex, 0.0).rgb);
    float normalWeight = max(0.0, dot(centerNormal, normal));
    float w = dim * exp(-abs(depth - centerDepth) * 4.) * pow(normalWeight, 16.0);
    return vec2(textureLod(aoInputTexture, tex, 0.0).r * w, w);
}

void main() {
    float occlusion = texture(aoInputTexture, vtex).r;
    float w = 1.;
    vec2 step = direction / screenWidth; // TODO

    float centerDepth = texture(depthGeometryTexture, vtex).r;
    vec3 centerNormal = normalize(texture(normalGeometryTexture, vtex).rgb);

    for (float i = 1.0; i <= radius; i++) {
        float dim = exp(- i * i / (radius * radius));
        vec2 p1 = blurPoint(vtex + step * i, dim, centerDepth, centerNormal);
        vec2 p2 = blurPoint(vtex - step * i, dim, centerDepth, centerNormal);
        occlusion += p1.x + p2.x;
        w += p1.y + p2.y;
    }
    fragColor = vec4(occlusion / w, centerDepth, 0.0, 1.0);
}
