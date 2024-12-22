#import "shader/lib/header.glsl"
#import "shader/lib/texturing.glsl"
#import "shader/lib/light.glsl"

#ifdef COLOR
  uniform vec4 color;
#else
  uniform sampler2D colorTexture;
#endif

uniform vec3 cameraPos;
uniform vec3 light;

uniform float ambient;
uniform float diffuse;
uniform float specular;
uniform float specularPower;

#ifdef TRIPLANAR
  uniform float triplanarScale;
#endif
#ifdef APERIODIC
  uniform sampler2D aperiodicTexture;
#endif
#ifdef NORMAL_MAP
  uniform sampler2D normalTexture;
#endif
#ifdef DETAIL
  uniform sampler2D detailTexture;
  uniform float detailScale;
  uniform float detailRatio;
#endif
#ifdef SHADOW_RECEIVER0
  uniform sampler2D shadowTexture0;
#endif
#ifdef SHADOW_RECEIVER1
  uniform sampler2D shadowTexture1;
#endif
#ifdef SHADOW_RECEIVER2
  uniform sampler2D shadowTexture2;
#endif

in vec3 mpos;
in vec3 mnormal;
in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef SHADOW_RECEIVER0
  in vec3 vshadow0;
#endif
#ifdef SHADOW_RECEIVER1
  in vec3 vshadow1;
#endif
#ifdef SHADOW_RECEIVER1
  in vec3 vshadow2;
#endif

#ifdef PLUGIN_TEXTURE
  #import "$texture"
#endif

out vec4 fragColor;

void main() {

  vec3 normal = normalize(vnormal);
  vec3 look = normalize(vpos - cameraPos);

  #ifdef NORMAL_MAP
    normal = perturbNormal(normal, normalTexture, vtex, look);
  #endif

  #ifdef PLUGIN_TEXTURE
    vec4 texColor = pluginTexture();
  #else
    #ifdef TRIPLANAR
       #ifdef APERIODIC
         vec4 texColor = triplanarAperiodic(colorTexture, aperiodicTexture, mpos * triplanarScale, mnormal);
       #else
         vec4 texColor = triplanar(colorTexture, mpos * triplanarScale, mnormal);
       #endif
    #else
       #ifdef APERIODIC
         vec4 texColor = aperiodic(colorTexture, aperiodicTexture, vtex);
       #else
         #ifdef COLOR
           vec4 texColor = color;
         #else
           vec4 texColor = texture(colorTexture, vtex);
         #endif
       #endif
    #endif
  #endif
  if (texColor.a < 0.01)
    discard;
  #ifdef DETAIL
    vec4 detailColor = texture(detailTexture, vtex * detailScale);
    texColor = mix(texColor, detailColor, detailRatio);
  #endif

  #ifdef NO_LIGHT
    float lighting = 1.0f;
  #else
    float lighting = lite(light, normal, look, ambient, diffuse, specular, specularPower);
  #endif

  float shadowRatio = 0.0f;

  #ifdef SHADOW_RECEIVER0
    shadowRatio = max(shadowRatio, shadow(shadowTexture0, vshadow0));
  #endif
  #ifdef SHADOW_RECEIVER1
    shadowRatio = max(shadowRatio, shadow(shadowTexture1, vshadow1));
  #endif
  #ifdef SHADOW_RECEIVER2
    shadowRatio = max(shadowRatio, shadow(shadowTexture2, vshadow2));
  #endif

  lighting = mix(lighting, ambient, shadowRatio);

  #ifdef SHADOW_CASTER
    fragColor = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
  #else
    fragColor = vec4(texColor.xyz * lighting, texColor.a);
  #endif
}