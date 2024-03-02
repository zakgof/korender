#version 130

#import "texturing.glsl"
#import "light.glsl"

uniform sampler2D baseTexture;

uniform sampler2D sandTexture;
uniform sampler2D rockTexture;
uniform sampler2D grassTexture;
uniform sampler2D dirtTexture;

uniform float detailScale;
uniform float detailRatio;
uniform float ambient;
uniform float diffuse;
uniform float specular;
uniform float specularPower;

uniform vec3 cameraPos;
uniform vec3 light;

in vec3 mpos;
in vec3 mnormal;
in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
in vec3 vshadow;

out vec4 fragColor;

void main() {

  vec3 normal = normalize(vnormal);
  vec3 look = normalize(vpos - cameraPos);

  #ifdef NORMAL_MAP
    normal = perturbNormal(normal, normalTexture, vtex, look);
  #endif

  vec4 texColor = texture2D(baseTexture, vtex);
  float x= texColor.r;


  vec4 sand = texture2D(sandTexture, vtex * detailScale);
  vec4 rock = texture2D(rockTexture, vtex * detailScale);
  vec4 grass = texture2D(grassTexture, vtex * detailScale);
  vec4 dirt = texture2D(dirtTexture, vtex * detailScale);

  float sandRatio  = clamp(1.0 - abs(x-0.10)*5., 0.1, 1.0) + max(8. - vpos.y, 0.) / 3.0 ;
  float rockRatio  = clamp(1.0 - abs(x-0.40)*5., 0.1, 1.0);
  float grassRatio = clamp(1.0 - abs(x-0.60)*5., 0.1, 1.0);
  float dirtRatio  = clamp(1.0 - abs(x-1.80)*5., 0.1, 1.0);
  float baseRatio = 0.4;

  float nrm = 1. / (sandRatio + rockRatio + grassRatio + dirtRatio + baseRatio);

  texColor = sand * sandRatio*nrm + rock * rockRatio*nrm + grass * grassRatio*nrm + dirt * dirtRatio*nrm + texColor*baseRatio*nrm;

  float lighting = lite(light, normal, look, ambient, diffuse, specular, specularPower);

  vec3 pixelColor = texColor.xyz * lighting;

//  float distance = length(vpos - cameraPos);
//  float fogFactor = clamp( distance / 1024.0, 0.0, 1.0);
//  vec3 fogColor = vec3(0.7, 0.7, 0.8);
//  pixelColor = mix(pixelColor, fogColor, fogFactor);

  fragColor = vec4(pixelColor, texColor.a);
}