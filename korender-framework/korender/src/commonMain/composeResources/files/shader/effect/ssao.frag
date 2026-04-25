#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D normalGeometryTexture;
uniform sampler2D depthGeometryTexture;

#uniform int sampleCount;
#uniform float radius;
#uniform float bias;
#uniform float intensity;

#uniforms

out vec4 fragColor;

#import "!shader/lib/space.glsl"

const int SSAO_MAX_SAMPLES = 32;

const vec3 ssaoKernel[SSAO_MAX_SAMPLES] = vec3[](
    vec3(0.5381, 0.1856, 0.4319),
    vec3(0.1379, 0.2486, 0.4430),
    vec3(0.3371, 0.5679, 0.0057),
    vec3(0.6999, 0.0451, 0.0019),
    vec3(0.0689, 0.8448, 0.5344),
    vec3(0.0560, 0.0069, 0.1843),
    vec3(0.0146, 0.1402, 0.0762),
    vec3(0.0100, 0.1920, 0.0344),
    vec3(0.3577, 0.5301, 0.4358),
    vec3(0.3169, 0.1063, 0.0185),
    vec3(0.0100, 0.9000, 0.0100),
    vec3(0.2214, 0.3808, 0.2018),
    vec3(0.4974, 0.4171, 0.3525),
    vec3(0.1754, 0.0783, 0.5596),
    vec3(0.0125, 0.0425, 0.1856),
    vec3(0.1681, 0.2887, 0.0064),
    vec3(0.7020, 0.1250, 0.2110),
    vec3(0.2440, 0.6210, 0.1320),
    vec3(0.8910, 0.2740, 0.0440),
    vec3(0.1160, 0.7130, 0.4270),
    vec3(0.4430, 0.0820, 0.6940),
    vec3(0.0570, 0.3340, 0.8230),
    vec3(0.8210, 0.5710, 0.1180),
    vec3(0.3080, 0.9030, 0.0610),
    vec3(0.6140, 0.2190, 0.5170),
    vec3(0.1670, 0.4980, 0.7590),
    vec3(0.9620, 0.3610, 0.2050),
    vec3(0.0820, 0.8330, 0.2980),
    vec3(0.3980, 0.1570, 0.8730),
    vec3(0.7410, 0.4420, 0.2910),
    vec3(0.2300, 0.6920, 0.5540),
    vec3(0.5140, 0.0640, 0.4380)
);

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    float depth = texture(depthGeometryTexture, vtex).r;
    if (depth >= 1.0) {
        fragColor = vec4(1.0);
        gl_FragDepth = depth;
        return;
    }

    vec3 viewPos = screenToViewSpace(vtex, depth).xyz;
    vec3 worldNormal = texture(normalGeometryTexture, vtex).rgb * 2.0 - 1.0;
    vec3 normal = normalize(mat3(view) * worldNormal);

    vec2 noiseUv = vtex * vec2(screenWidth, screenHeight) / 4.0;
    vec3 randomVec = normalize(vec3(
        hash12(noiseUv),
        hash12(noiseUv + vec2(13.7, 3.1)),
        hash12(noiseUv + vec2(7.9, 11.3))
    ) * 2.0 - 1.0);

    vec3 tangent = randomVec - normal * dot(randomVec, normal);
    if (length(tangent) < 1e-3) {
        tangent = abs(normal.z) < 0.999 ? cross(normal, vec3(0.0, 0.0, 1.0)) : cross(normal, vec3(0.0, 1.0, 0.0));
    }
    tangent = normalize(tangent);
    vec3 bitangent = normalize(cross(normal, tangent));
    mat3 tbn = mat3(tangent, bitangent, normal);

    int samples = clamp(sampleCount, 1, SSAO_MAX_SAMPLES);
    float occlusion = 0.0;
    for (int i = 0; i < SSAO_MAX_SAMPLES; i++) {
        if (i >= samples) {
            break;
        }

        float scale = mix(0.15, 1.0, pow(float(i) / max(float(samples - 1), 1.0), 2.0));
        vec3 samplePos = viewPos + tbn * ssaoKernel[i] * (radius * scale);
        vec4 sampleClip = pluginVProjection(samplePos);
        if (sampleClip.w <= 0.0) {
            continue;
        }
        vec2 sampleUv = sampleClip.xy / sampleClip.w * 0.5 + 0.5;

        if (sampleUv.x < 0.0 || sampleUv.x > 1.0 || sampleUv.y < 0.0 || sampleUv.y > 1.0) {
            continue;
        }

        float sampleDepth = texture(depthGeometryTexture, sampleUv).r;
        if (sampleDepth >= 1.0) {
            continue;
        }

        vec3 sampleViewPos = screenToViewSpace(sampleUv, sampleDepth).xyz;
        float diff = sampleViewPos.z - samplePos.z;
        float rangeCheck = smoothstep(0.0, 1.0, radius / (abs(diff) + 1e-4));
        occlusion += step(bias, diff) * rangeCheck;
    }

    float ao = 1.0 - (occlusion / float(samples)) * intensity;
    ao = clamp(pow(ao, 1.35), 0.0, 1.0);
    fragColor = vec4(vec3(ao), 1.0);
    gl_FragDepth = depth;
}
