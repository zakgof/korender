#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

#ifdef VERTEX_COLOR
    in vec4 vcolor;
#endif
#ifdef VERTEX_METALLIC
    in float vmetallic;
#endif
#ifdef VERTEX_ROUGHNESS
    in float vroughness;
#endif
#ifdef VERTEX_OCCLUSION
    in float vocclusion;
#endif

#uniform vec4 baseColor;

#uniform float metallicFactor;
#uniform float roughnessFactor;
#uniform float alphaCutoff;

uniform sampler2D shadowTextures[5];
uniform sampler2DShadow pcfTextures[5];

#uniforms

out vec4 fragColor;

float shadowRatios[5] = float[5](0., 0., 0., 0., 0.);

vec3 position;
vec4 albedo;
vec3 normal;
vec3 emission;
float metallic;
float roughness;
vec3 color;
vec3 look;

#ifdef PLUGIN_POSITION
    #import "$position"
#endif

#ifdef PLUGIN_NORMAL
    #import "$normal"
#endif

#ifdef PLUGIN_TEXSOURCE
    #import "$texsource"
    #ifndef PLUGIN_TEXTURING
        #import "!shader/plugin/texturing.default.frag"
    #else
        #import "$texturing"
    #endif
#endif

#ifdef PLUGIN_ALBEDO
    #import "$albedo"
#endif

#ifdef PLUGIN_DISCARD
    #import "$discard"
#endif

#ifdef PLUGIN_EMISSION
    #import "$emission"
#endif

#ifdef PLUGIN_METALLIC_ROUGHNESS
    #import "$metallic_roughness"
#endif

#ifdef PLUGIN_SPECULAR_GLOSSINESS
    #import "$specular_glossiness"
#endif

#ifdef PLUGIN_OUTPUT
    #import "$output"
#endif

#ifndef SHADOW_CASTER
    #ifdef PLUGIN_DEPTH
        #import "$depth"
    #endif
#endif

#ifdef PLUGIN_OCCLUSION
    #import "$occlusion"
#endif

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef PLUGIN_SKY
    #import "!shader/lib/space.glsl"
    #import "!shader/lib/sky.glsl"
    #import "$sky"
    #import "!shader/lib/skyibl.glsl"
#endif

void main() {

    albedo = baseColor;

    #ifdef VERTEX_COLOR
        albedo *= vcolor;
    #endif

    #ifdef PLUGIN_POSITION
        position = pluginPosition();
    #else
        position = vpos;
    #endif

    #ifdef PLUGIN_NORMAL
        normal = pluginNormal();
    #else
        normal = normalize(vnormal);
    #endif

    #ifdef PLUGIN_TEXSOURCE
        albedo *= pluginTexturing();
    #endif

    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo();
    #endif

    #ifdef PLUGIN_DISCARD
        if (pluginDiscard())
            discard;
    #else
        if (albedo.a < alphaCutoff)
            discard;
    #endif

    #ifdef SHADOW_CASTER
        #ifdef VSM_SHADOW
            float d = gl_FragCoord.z;
            float m1 = d * d;
            float dx = dFdx(m1);
            float dy = dFdy(m1);
            float m2 = m1 * m1 + 0.25 * (dx * dx + dy * dy);
            fragColor = vec4(m1, m2, 1.0, 1.0);
        #endif
    #else
        emission = vec3(0.);
        #ifdef PLUGIN_EMISSION
            emission = pluginEmission();
        #endif

        metallic = metallicFactor;
        roughness = roughnessFactor;

        #ifdef PLUGIN_METALLIC_ROUGHNESS
            vec2 mr = pluginMetallicRoughness();
            metallic = mr.x;
            roughness = mr.y;
        #endif

        #ifdef PLUGIN_SPECULAR_GLOSSINESS
            vec4 sg = pluginSpecularGlossiness();
            float maxSpec = max(max(sg.r, sg.g), sg.b);
            metallic = clamp((maxSpec - 0.04) / (1.0 - 0.04), 0.0, 1.0);
            if (metallic > 0.01) {
                albedo.rgb = sg.rgb;
            }
            roughness = 1. - sg.a;
        #endif

        #ifdef VERTEX_METALLIC
            metallic = vmetallic;
        #endif
        #ifdef VERTEX_ROUGHNESS
            roughness = vroughness;
        #endif

        ///////////////////////

        look = normalize(cameraPos - position);
        float plane = dot((position - cameraPos), cameraDir);

        float occlusion = 1.0;
        #ifdef VERTEX_OCCLUSION
            occlusion *= vocclusion;
        #endif
        #ifdef PLUGIN_OCCLUSION
            occlusion *= pluginOcclusion();
        #endif

        populateShadowRatios(plane, position);

        vec3 F0 = mix(vec3(0.04), albedo.rgb, metallic);
        float NdotV = max(dot(look, normal), 0.0);
        float alpha = roughness * roughness;
        float alpha2 = alpha * alpha;
        float k = (roughness + 1.0) * (roughness + 1.0) / 8.0;
        float ggxV = NdotV / (NdotV * (1.0 - k) + k);

        color = emission;

        for (int l=0; l<numDirectionalLights; l++) {
            color += dirLight(l, normal, look, albedo.rgb, metallic, alpha2, k, ggxV, F0, occlusion);
        }
        for (int l=0; l<numPointLights; l++) {
            color += pointLight(position, l, normal, look, albedo.rgb, metallic, alpha2, k, ggxV, F0, occlusion);
        }

        vec3 diffFactor = albedo.rgb * (1.0 - metallic);
        color += ambientColor * diffFactor;

        #ifdef PLUGIN_SKY
            color += skyibl(normal, look, roughness, diffFactor, F0, NdotV);
        #else
            // Fallback for ambient-only setups: keep metallic surfaces from going black without IBL.
            color += ambientColor * F0 * metallic;
        #endif

        #ifdef PLUGIN_OUTPUT
            fragColor = pluginOutput();
        #else
            fragColor = vec4(color * albedo.a, albedo.a);
        #endif

        #ifdef PLUGIN_DEPTH
            gl_FragDepth = pluginDepth();
        #endif

    #endif
}
