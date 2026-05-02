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

const int HBAO_MAX_DIRECTIONS = 16;
const int HBAO_MAX_STEPS = 8;

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
    float rotation = hash12(noiseUv) * 6.2831853;

    vec3 tangent = vec3(cos(rotation), sin(rotation), 0.0);
    tangent = tangent - normal * dot(tangent, normal);
    if (length(tangent) < 1e-3) {
        tangent = abs(normal.z) < 0.999 ? cross(normal, vec3(0.0, 0.0, 1.0)) : cross(normal, vec3(0.0, 1.0, 0.0));
    }
    tangent = normalize(tangent);
    vec3 bitangent = normalize(cross(normal, tangent));
    mat3 tbn = mat3(tangent, bitangent, normal);

    int directions = clamp(sampleCount / 2, 4, HBAO_MAX_DIRECTIONS);
    int steps = clamp(sampleCount / directions, 2, HBAO_MAX_STEPS);

    float occlusion = 0.0;
    for (int d = 0; d < HBAO_MAX_DIRECTIONS; d++) {
        if (d >= directions) {
            break;
        }

        float angle = rotation + 6.2831853 * (float(d) / float(directions));
        vec3 dir = normalize(tbn * vec3(cos(angle), sin(angle), 0.0));

        float horizon = -1.5707963;
        for (int s = 0; s < HBAO_MAX_STEPS; s++) {
            if (s >= steps) {
                break;
            }

            float t = (float(s) + 1.0) / float(steps);
            float dist = radius * t;
            vec3 samplePos = viewPos + dir * dist;
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
            vec3 delta = sampleViewPos - viewPos;
            float height = dot(delta, normal);
            float radial = length(delta - normal * height);
            if (radial < 1e-4) {
                continue;
            }

            float horizonAngle = atan(height, radial);
            horizon = max(horizon, horizonAngle);
        }

        float localOcclusion = smoothstep(bias, 1.35, horizon);
        localOcclusion *= 1.0 - float(d) / float(max(directions - 1, 1));
        occlusion += localOcclusion;
    }

    float ao = 1.0 - (occlusion / float(directions)) * intensity;
    ao = clamp(pow(ao, 1.35), 0.0, 1.0);
    fragColor = vec4(vec3(ao), 1.0);
    gl_FragDepth = depth;
}
