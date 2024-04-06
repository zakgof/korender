#import "lib/header.glsl"
#import "lib/texturing.glsl"
#import "lib/light.glsl"

#ifdef COLOR
  uniform vec3 color;
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
#ifdef SHADOW_RECEIVER
  uniform sampler2D shadowTexture;
#endif

in vec3 mpos;
in vec3 mnormal;
in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef SHADOW_RECEIVER
  in vec3 vshadow;
#endif

out vec4 fragColor;

void main() {

  vec3 normal = normalize(vnormal);
  vec3 look = normalize(vpos - cameraPos);

  #ifdef NORMAL_MAP
    normal = perturbNormal(normal, normalTexture, vtex, look);
  #endif

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
         vec4 texColor = vec4(color, 1.0);
       #else
         vec4 texColor = texture(colorTexture, vtex);
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

  #ifdef SHADOW_RECEIVER
    float shadowRatio = shadow(shadowTexture, vshadow);
    lighting = mix(lighting, ambient, shadowRatio);
  #endif

  #ifdef SHADOW_CASTER
    fragColor = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
  #else
    fragColor = vec4(texColor.xyz * lighting, texColor.a);
  #endif

//  #ifdef SHADOW_RECEIVER
//
//    if (vshadow.x > 0.0 && vshadow.x < 1.0 && vshadow.y > 0.0 && vshadow.y < 1.0) {
//
//      float shadowSample = texture(shadowTexture, vshadow.xy).r;
//      float sh = (shadowSample > vshadow.z + 0.01) ? 1.0 : 0.0;
//
//
//      fragColor = vec4(0.0, 0.0, vshadow.z, 1.0);
//    }
//  #endif


}