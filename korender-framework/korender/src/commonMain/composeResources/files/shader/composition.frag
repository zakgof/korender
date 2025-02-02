#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;
uniform sampler2D emissionTexture;
uniform sampler2D depthTexture;

//////////

uniform vec3 cameraPos;
uniform vec3 cameraDir;
uniform vec3 ambientColor;
uniform mat4 projection;
uniform mat4 view;

//////////

const int MAX_LIGHTS = 32;
uniform int numDirectionalLights;
uniform vec3 directionalLightDir[MAX_LIGHTS];
uniform vec3 directionalLightColor[MAX_LIGHTS];
uniform int directionalLightShadowTextureIndex[MAX_LIGHTS];
uniform int directionalLightShadowTextureCount[MAX_LIGHTS];

uniform int numPointLights;
uniform vec3 pointLightPos[MAX_LIGHTS];
uniform vec3 pointLightColor[MAX_LIGHTS];
uniform vec3 pointLightAttenuation[MAX_LIGHTS];

const int MAX_SHADOWS = 8;
uniform int numShadows;
uniform sampler2D shadowTextures[MAX_SHADOWS];
uniform mat4 bsps[MAX_SHADOWS];
uniform vec4 cascade[MAX_SHADOWS];
uniform float yMin[MAX_SHADOWS];
uniform float yMax[MAX_SHADOWS];
uniform int shadowMode[MAX_SHADOWS];
uniform float f1[MAX_SHADOWS];
uniform int i1[MAX_SHADOWS];

//////////

out vec4 fragColor;

float shadowRatios[MAX_SHADOWS];

#import "!shader/lib/space.glsl"
#import "!shader/lib/ssr.glsl"
#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

#ifdef PLUGIN_COLOR
#import "$color"
#endif

#ifdef PLUGIN_DEPTH
#import "$depth"
#endif

void main() {

    float depth = texture(depthTexture, vtex).r;

    vec4 viewPos = screenToViewSpace(vtex, depth);
    vec3 vpos = (inverse(view) * viewPos).xyz;

    vec3 c_diff = texture(cdiffTexture, vtex).rgb;
    vec4 materialTexel = texture(materialTexture, vtex);
    vec4 emissionTexel = texture(emissionTexture, vtex);
    vec4 normalTexel = texture(normalTexture, vtex);

    vec3 F0 = materialTexel.rgb;
    float rough = materialTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    vec3 color = c_diff * ambientColor.rgb + emissionTexel.rgb;

    float plane = dot((vpos - cameraPos), cameraDir);
    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++)
        color += dirLight(l, N, V, c_diff, F0, rough);

    for (int l=0; l<numPointLights; l++)
        color += pointLight(vpos, l, N, V, c_diff, F0, rough);

#ifdef PLUGIN_COLOR
    color = pluginColor(vpos, color, depth);
#endif

#ifdef PLUGIN_DEPTH
    color = pluginDepth(vpos, color, depth);
#endif

    // vec3 reflection = ssr(viewPos.xyz, N);
    // float fresnel = pow(1.0 - dot(N, normalize(viewPos.xyz)), 5.0);
    // color += reflection * F0 * fresnel;

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}