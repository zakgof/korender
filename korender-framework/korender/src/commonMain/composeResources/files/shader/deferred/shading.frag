#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D diffuseGeometryTexture;
uniform sampler2D normalGeometryTexture;
uniform sampler2D materialGeometryTexture;
uniform sampler2D emissionGeometryTexture;
uniform sampler2D depthGeometryTexture;

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

const int MAX_SHADOWS = 5;
uniform int numShadows;
uniform sampler2D shadowTextures[MAX_SHADOWS];
uniform sampler2DShadow pcfTextures[MAX_SHADOWS];
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

#ifdef PLUGIN_COLOR
#import "$color"
#endif

#ifdef PLUGIN_DEPTH
#import "$depth"
#endif

#import "!shader/lib/space.glsl"

#ifdef PLUGIN_SKY
#import "!shader/lib/sky.glsl"
#import "$sky"
#endif

#import "!shader/lib/shadow.glsl"
#import "!shader/lib/pbr.glsl"
#import "!shader/lib/light.glsl"

void main() {

    float depth = texture(depthGeometryTexture, vtex).r;

    vec3 vpos = screenToWorldSpace(vtex, depth);

    vec3 c_diff = texture(diffuseGeometryTexture, vtex).rgb;
    vec4 materialTexel = texture(materialGeometryTexture, vtex);
    vec4 emissionTexel = texture(emissionGeometryTexture, vtex);
    vec4 normalTexel = texture(normalGeometryTexture, vtex);

    vec3 F0 = materialTexel.rgb;
    float rough = materialTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    vec3 color = c_diff * ambientColor.rgb + emissionTexel.rgb;

    float plane = dot((vpos - cameraPos), cameraDir);
    populateShadowRatios(plane, vpos);

    for (int l=0; l<numDirectionalLights; l++)
        color += dirLight(l, N, V, c_diff, F0, rough, 1.0);

    for (int l=0; l<numPointLights; l++)
        color += pointLight(vpos, l, N, V, c_diff, F0, rough, 1.0);

#ifdef PLUGIN_COLOR
    color = pluginColor(vpos, color, depth);
#endif

#ifdef PLUGIN_DEPTH
    color = pluginDepth(vpos, color, depth);
#endif

    fragColor = vec4(color, 1.);
    gl_FragDepth = depth;
}