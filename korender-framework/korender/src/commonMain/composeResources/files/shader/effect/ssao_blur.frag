#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D aoInputTexture;
uniform sampler2D depthGeometryTexture;
uniform sampler2D normalGeometryTexture;

#uniform vec2 direction;
#uniform int downsample;
#uniform float radius;

#uniforms

out vec4 fragColor;

float bilateralWeight(float depthCenter, float depthSample, vec3 normalCenter, vec3 normalSample, float offset) {
    float depthDiff = abs(depthSample - depthCenter);
    float spatial = exp(-(offset * offset) / max(radius * radius, 1.0));
    float depthFactor = exp(-depthDiff * 220.0);
    float normalFactor = pow(max(dot(normalCenter, normalSample), 0.0), 8.0);
    return spatial * depthFactor * normalFactor;
}

void main() {
    float centerDepth = textureLod(depthGeometryTexture, vtex, 0.0).r;
    vec3 centerNormal = textureLod(normalGeometryTexture, vtex, 0.0).rgb * 2.0 - 1.0;
    centerNormal = normalize(centerNormal);
    vec2 step = direction * float(downsample) / vec2(screenWidth, screenHeight);

    float ao = textureLod(aoInputTexture, vtex, 0.0).r;
    float weight = 1.0;

    for (float i = 1.0; i <= radius; i++) {
        vec2 uv1 = vtex + step * i;
        vec2 uv2 = vtex - step * i;

        if (uv1.x >= 0.0 && uv1.x <= 1.0 && uv1.y >= 0.0 && uv1.y <= 1.0) {
            float depth1 = textureLod(depthGeometryTexture, uv1, 0.0).r;
            vec3 normal1 = normalize(textureLod(normalGeometryTexture, uv1, 0.0).rgb * 2.0 - 1.0);
            float w1 = bilateralWeight(centerDepth, depth1, centerNormal, normal1, i);
            ao += textureLod(aoInputTexture, uv1, 0.0).r * w1;
            weight += w1;
        }

        if (uv2.x >= 0.0 && uv2.x <= 1.0 && uv2.y >= 0.0 && uv2.y <= 1.0) {
            float depth2 = textureLod(depthGeometryTexture, uv2, 0.0).r;
            vec3 normal2 = normalize(textureLod(normalGeometryTexture, uv2, 0.0).rgb * 2.0 - 1.0);
            float w2 = bilateralWeight(centerDepth, depth2, centerNormal, normal2, i);
            ao += textureLod(aoInputTexture, uv2, 0.0).r * w2;
            weight += w2;
        }
    }

    fragColor = vec4(vec3(ao / max(weight, 1e-4)), 1.0);
    gl_FragDepth = centerDepth;
}
