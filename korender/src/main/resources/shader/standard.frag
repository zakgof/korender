#version 130

#import "texturing.glsl"
#import "light.glsl"

uniform sampler2D colorTexture;
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
       vec4 texColor = texture2D(colorTexture, vtex);
     #endif
  #endif
  if (texColor.a < 0.01)
    discard;
  #ifdef DETAIL
    vec4 detailColor = texture2D(detailTexture, vtex * detailScale);
    texColor = mix(texColor, detailColor, detailRatio);
  #endif

  float lighting = lite(light, normal, look, ambient, diffuse, specular, specularPower);

  #ifdef SHADOW_RECEIVER
    float shadowSample = texture2D(shadowTexture, vshadow.xy).r;
    if (shadowSample  >=  vshadow.z && vshadow.z > 0.0) {
       lighting = ambient;
    }
  #endif

  #ifdef NO_LIGHT
    lighting = 1.0f;
  #endif

  #ifdef SHADOW_CASTER
    fragColor = vec4(gl_FragCoord.z, gl_FragCoord.z, gl_FragCoord.z, 1.0);
  #else
    fragColor = vec4(texColor.xyz * lighting, texColor.a);
  #endif

}