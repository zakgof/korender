#version 130

#import "texturing.glsl"

uniform sampler2D textureMap;
uniform vec3 cameraPos;

uniform float ambient;
uniform float diffuse;
uniform float specular;
uniform float specularPower;

#ifdef TRIPLANAR
  uniform float triplanarScale;
#endif
#ifdef APERIODIC
  uniform sampler2D tileIndexMap;
#endif

in vec3 mpos;
in vec3 mnormal;
in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

out vec4 fragColor;

void main() {
  vec3 light = normalize(vec3(1, 0, 0));

  vec3 look = normalize(vpos - cameraPos);

  float diffuseRatio = diffuse * clamp(dot(-light, vnormal), 0.0, 1.0);
  float specRatio = specular * pow(clamp(dot(reflect(-light, vnormal), look), 0.0, 1.0), specularPower);

  #ifdef TRIPLANAR
	vec4 texColor = triplanar(textureMap, mpos * triplanarScale, mnormal);
  #else
     #ifdef APERIODIC
	   vec4 texColor = aperiodic(textureMap, tileIndexMap, vtex);
     #else
       vec4 texColor = texture2D(textureMap, vtex);
     #endif
  #endif

  fragColor = texColor * (ambient + diffuseRatio + specRatio);
}