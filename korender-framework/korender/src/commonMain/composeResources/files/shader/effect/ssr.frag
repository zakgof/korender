#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec2 vtex;

uniform sampler2D colorInputTexture;

uniform sampler2D normalGeometryTexture;
uniform sampler2D materialGeometryTexture;
uniform sampler2D depthGeometryTexture;

uniform sampler2D noiseTexture;

#uniform float startStep;
#uniform float nextStepRatio;
#uniform float maxReflectionDistance;
#uniform int linearSteps;
#uniform int binarySteps;

#uniforms

#ifdef SSR_ENV
    uniform samplerCube envTexture;
#endif

out vec4 fragColor;

#import "!shader/lib/space.glsl"

vec3 wToS(vec3 r) {
    vec4 p = pluginVProjection((view * vec4(r, 1.0)).xyz);
    return (vec3(1.) + p.xyz / p.w) * 0.5;
}

vec4 ssr(vec3 vpos, vec3 N, vec3 V, float roughness) {

    vec3 rayDir = normalize(reflect(-V, N));

#ifdef SSR_ENV
    vec4 dflt = vec4(texture(envTexture, rayDir).rgb, 1.);
#else
    vec4 dflt =  vec4(0., 0., 0., 1.);
#endif

    float step = startStep;
    float w = 1.;
    float peel = 0.01;

    // Bent normals
    float bendAmount = 0.6;             // tweak 0.1 – 0.6
    float f = (1.0 - roughness);        // 1=mirror, 0=diffuse
    vec3 bentNormal = normalize(mix(N, V, f * bendAmount));

    // Adaptive bias
    float angle = abs(dot(N, V));
    float bias = mix(0.5 * 0.2, 0.5, 1.0 - angle);  // more bias for grazing
    bias = mix(bias, bias * 0.5, roughness);  // glossy reduces bias slightly

    vec3 rayPoint = vpos + bentNormal * bias;
    vec3 rayStep = rayDir * step;
    float travel = 0.;

    float startOffset = textureLod(noiseTexture, vtex * 1.0, 0.0).r * 1.0;
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
            vec3 clr =  mix(dflt.rgb, texture(colorInputTexture, uv.xy).rgb, w);
            float dpth = texture(depthGeometryTexture, uv.xy).r;
            return vec4(clr, dpth);
        }

        step *= nextStepRatio;
        rayStep *= nextStepRatio;
    }
    return dflt;
}

void main() {

    float depth = texture(depthGeometryTexture, vtex).r;
    vec3 vpos = screenToWorldSpace(vtex, depth);

    vec4 materialTexel = texture(materialGeometryTexture, vtex);
    vec4 normalTexel = texture(normalGeometryTexture, vtex);

    vec3 F0 = materialTexel.rgb;
    float rough = materialTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    vec4 reflection = vec4(0.);
    if (depth < 1.0) {
        reflection = ssr(vpos, N, V, rough);
        float NdotV = clamp(dot(N, V), 0.0, 1.0);
        vec3 FR = F0 + (1. - F0) * pow(1. - NdotV, 5.);
        reflection.rgb *= FR * (1. - rough);
    }

    fragColor = vec4(reflection.rgb, 1.0);

    vec2 offset = 1.0 / vec2(textureSize(depthGeometryTexture, 0));
    float d1 = texture(depthGeometryTexture, vtex + offset * vec2(0., 1.)).r;
    float d2 = texture(depthGeometryTexture, vtex + offset * vec2(0., -1.)).r;
    float d3 = texture(depthGeometryTexture, vtex + offset * vec2(-1., 0.)).r;
    float d4 = texture(depthGeometryTexture, vtex + offset * vec2(1., 0.)).r;

    gl_FragDepth = (depth + d1 + d2 + d3 + d4) * 0.2; // min(depth, min(min(d1, d2), min(d3, d4)));
}