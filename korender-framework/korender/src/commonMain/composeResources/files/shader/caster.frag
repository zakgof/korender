#import "!shader/lib/header.glsl"
#import "!shader/lib/ubo.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef VERTEX_COLOR
    in vec4 vcolor;
#endif

#uniform vec4 baseColor;
#ifdef BASE_COLOR_MAP
    uniform sampler2D baseColorTexture;
#endif

#uniforms

out vec4 fragColor;

vec3 position;
vec4 albedo;

#ifdef PLUGIN_POSITION
#import "$position"
#endif

#ifdef PLUGIN_TEXTURING
#import "$texturing"
#endif

#ifdef PLUGIN_ALBEDO
#import "$albedo"
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

    #ifdef PLUGIN_TEXTURING
        albedo *= pluginTexturing();
    #else
        #ifdef BASE_COLOR_MAP
            albedo *= texture(baseColorTexture, vtex);
        #endif
    #endif

    #ifdef PLUGIN_ALBEDO
        albedo = pluginAlbedo();
    #endif

    if (albedo.a < 0.001)
        discard;


#ifdef VSM_SHADOW
    float d = gl_FragCoord.z;
    float m1 = d * d;
    float dx = dFdx(m1);
    float dy = dFdy(m1);
    float m2 = m1 * m1 + 0.25 * (dx * dx + dy * dy);
    fragColor = vec4(m1, m2, 1.0, 1.0);
#endif

}