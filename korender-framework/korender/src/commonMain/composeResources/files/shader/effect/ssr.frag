#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"
#import "!shader/lib/pbr.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;
uniform sampler2D albedoGeometryTexture;
uniform sampler2D normalGeometryTexture;
uniform sampler2D depthGeometryTexture;

uniform sampler2D noiseTexture;

#uniform float startStep;
#uniform float nextStepRatio;
#uniform float maxReflectionDistance;
#uniform int linearSteps;
#uniform int binarySteps;

#uniforms

out vec4 fragColor;

#import "!shader/lib/space.glsl"

vec3 wToS(vec3 r) {
    vec4 p = pluginVProjection((view * vec4(r, 1.0)).xyz);
    return (vec3(1.) + p.xyz / p.w) * 0.5;
}

vec4 ssr(vec3 vpos, vec3 N, vec3 V, float roughness) {

    vec3 rayDir = normalize(reflect(-V, N));

    float step = startStep;
    float w = 1.;
    float peel = 0.01;

    // Bent normals
    float bendAmount = 0.1;             // tweak 0.1 – 0.6
    float f = (1.0 - roughness);        // 1=mirror, 0=diffuse
    vec3 bentNormal = normalize(mix(N, V, f * bendAmount));

    // Adaptive bias
    float angle = abs(dot(N, V));
    float bias = mix(startStep * 0.2, startStep, 1.0 - angle);  // more bias for grazing
    bias = mix(bias, bias * 0.5, roughness);  // glossy reduces bias slightly

    vec3 rayPoint = vpos + bentNormal * bias;
    vec3 rayStep = rayDir * step;
    float travel = 0.;

    float startOffset = textureLod(noiseTexture, vtex * 1.0, 0.0).r * 0.5;
    rayPoint -= rayStep * startOffset;

    for (int i = 0; i < linearSteps; i++) {

        vec3 prevPoint = rayPoint;
        rayPoint += rayStep;
        travel += step;

        vec3 uv = wToS(rayPoint);
        if (uv.x < 0. || uv.x > 1. || uv.y < 0. || uv.y > 1. || uv.z < 0. || uv.z > 1.) {
            break;
        }

        float deepen = uv.z - texture(depthGeometryTexture, uv.xy).r;

        if (deepen > peel)
            continue;

        if (deepen > peel * 0.1 && deepen < peel) {
            vec3 r2 = rayPoint;
            vec3 r1 = prevPoint;
            rayPoint = (r1 + r2) * 0.5;
            for (int b = 0; b < binarySteps; b++) {
                uv = wToS(rayPoint);
                deepen = uv.z - texture(depthGeometryTexture, uv.xy).r;
                if (deepen > 0.) {
                    r2 = rayPoint;
                } else {
                    r1 = rayPoint;
                }
                rayPoint = (r1 + r2) * 0.5;
            }
            uv = wToS(rayPoint);
            w *= smoothstep (peel, 0.0, abs(deepen));
            w *= smoothstep(maxReflectionDistance, 0., travel);
            return vec4(texture(colorInputTexture, uv.xy /*, 8. * roughness*/).rgb, w);
        }

        step *= nextStepRatio;
        rayStep *= nextStepRatio;
    }
    return vec4(0.);
}

void main() {

    float depth = texture(depthGeometryTexture, vtex).r;
    vec4 reflection = vec4(0.);
    if (depth < 1.0) {
        vec3 vpos = screenToWorldSpace(vtex, depth);

        vec4 albedoTexel = texture(albedoGeometryTexture, vtex);
        vec4 normalTexel = texture(normalGeometryTexture, vtex);

        vec3 albedo = albedoTexel.rgb;
        float metallic = albedoTexel.a;
        vec3 F0 = mix(vec3(0.04), albedo, metallic);
        float rough = normalTexel.a;

        vec3 V = normalize(cameraPos - vpos);
        vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);
        reflection = ssr(vpos, N, V, rough);
        float NdotV = clamp(dot(N, V), 0.0, 1.0);
        vec3 FR = fresnelSchlick(NdotV, F0);
        reflection.rgb *= FR;
    }
    fragColor = reflection;
}