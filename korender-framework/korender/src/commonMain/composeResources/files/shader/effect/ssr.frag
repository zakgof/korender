#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D colorTexture;

uniform sampler2D normalGeometryTexture;
uniform sampler2D materialGeometryTexture;
uniform sampler2D depthGeometryTexture;

#uniform float maxRayTravel;
#uniform int linearSteps;
#uniform int binarySteps;

#uniforms

#ifdef SSR_ENV
    uniform samplerCube envTexture;
#endif

out vec4 fragColor;

#import "!shader/lib/space.glsl"

vec3 wToS(vec3 r) {
    vec4 p = projection * (view * vec4(r, 1.0));
    return (vec3(1.) + p.xyz / p.w) * 0.5;
}

vec3 ssr(vec3 vpos, vec3 N, vec3 V) {

    vec3 rayDir = normalize(reflect(-V, N));

#ifdef SSR_ENV
    vec3 dflt = texture(envTexture, rayDir).rgb;
#else
    vec3 dflt =  vec3(0.);
#endif

    float w = 1.;



    float peel = 0.01;

    float travel = 0.;
    float step = maxRayTravel / float(linearSteps);
    vec3 rayPoint = vpos;
    vec3 rayStep = rayDir * step;

    for (int i = 0; i < linearSteps; i++) {

        vec3 prevPoint = rayPoint;
        rayPoint += rayStep;
        travel += step;

        vec3 uv = wToS(rayPoint);
        if (uv.x < 0. || uv.x > 1. || uv.y < 0. || uv.y > 1. || uv.z < 0. || uv.z > 1.)
            break;

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
            w *= smoothstep(maxRayTravel * maxRayTravel, 0., travel * travel);
            return mix(dflt, texture(colorTexture, uv.xy).rgb, w);
        }
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

    vec3 reflection = vec3(0.);
    if (depth < 1.0) {
        reflection = ssr(vpos, N, V);
        float NdotV = clamp(dot(N, V), 0.0, 1.0);
        vec3 FR = F0 + (1. - F0) * pow(1. - NdotV, 5.);
        reflection *= FR * (1. - rough);
    }

    fragColor = vec4(reflection, 1.0);
    gl_FragDepth = depth;
}