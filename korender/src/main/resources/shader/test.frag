#version 130

#import "texturing.glsl"

uniform sampler2D textureMap;
uniform vec3 cameraPos;

#ifdef TRIPLANAR
  uniform float triplanarScale;
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
  float ambient = 0.3;
  float diffuse = 0.7 * clamp(dot(-light, vnormal), 0.0, 1.0);
  float spec = 1.2 * pow(clamp(dot(reflect(-light, vnormal), look), 0.0, 1.0), 6.0);

  #ifdef TRIPLANAR
	vec4 texColor = triplanar(textureMap, mpos * triplanarScale, mnormal);
  #else
	vec4 texColor = texture2D(textureMap, vtex);
  #endif

  fragColor = texColor * (diffuse + ambient + spec);
}