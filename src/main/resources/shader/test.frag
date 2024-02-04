#version 130

uniform sampler2D textureMap;

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;

out vec4 fragColor;

void main() {
	vec4 light = vec4(vnormal * 0.001, 0);
  	fragColor = texture2D(textureMap, vtex) + light;
}