#import "!shader/lib/header.glsl"
#import "!shader/lib/shading.glsl"
#import "!shader/lib/pbr.glsl"

in vec2 vtex;

uniform sampler2D albedoTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;
uniform sampler2D depthTexture;

uniform vec3 cameraPos;
uniform vec4 ambientColor;
uniform mat4 projection;
uniform mat4 view;


struct DirectionalLight {
    vec3 dir;
    vec4 color;
};
struct PointLight {
    vec3 pos;
    vec4 color;
};

uniform DirectionalLight directionalLights[32];
uniform int numDirectionalLights;
uniform PointLight pointLights[32];
uniform int numPointLights;

out vec4 fragColor;

void main() {


    float depth = texture(depthTexture, vtex).r;

    vec4 ndcPosition;
    ndcPosition.xy = vtex * 2.0 - 1.0; // Convert to range [-1, 1]
    ndcPosition.z = depth * 2.0 - 1.0; // Depth range [-1, 1]
    ndcPosition.w = 1.0;
    vec4 viewPosition = inverse(projection) * ndcPosition; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    vec4 worldPosition4 = inverse(view) * viewPosition;
    vec3 vpos = worldPosition4.xyz;

    vec3 V = normalize(cameraPos - vpos);

    vec4 albedo = texture(albedoTexture, vtex);
    vec3 N = normalize(texture(normalTexture, vtex).rgb * 2.0 - 1.0);
    vec4 materialTexel = texture(materialTexture, vtex);

    float metal = materialTexel.r;
    float rough = materialTexel.g;

    float shadowRatio = 0.;

    vec3 color = albedo.rgb * ambientColor.rgb;

    for (int l=0; l<numDirectionalLights; l++) {
        vec3 lightValue = directionalLights[l].color.rgb * (1. - shadowRatio);
        vec3 L = normalize(-directionalLights[l].dir);

        color += calculatePBR(N, V, L, albedo.rgb, metal, rough, lightValue, 1., vec3(0.));
    }
    for (int l=0; l<numPointLights; l++) {
        vec3 ftol = pointLights[l].pos - vpos;
        float distance = length(ftol);
        float att = max(2.0, 3.0 / distance);
        vec3 lightValue = pointLights[l].color.rgb * (1. - shadowRatio) * att;// TODO quadratic; configurable attenuation ratio
        vec3 L = normalize(ftol);
        color += calculatePBR(N, V, L, albedo.rgb, metal, rough, lightValue, 1., vec3(0.));
    }


    // TODO: lighting and shadowing
    fragColor = vec4(color, albedo.a);
    gl_FragDepth = depth;
}