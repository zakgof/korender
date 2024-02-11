#import "header.glsl"

in vec2 tex;
in vec3 pos;

uniform mat4 model; 
uniform mat4 view;
uniform mat4 projection;

uniform vec3 cameraPos;
uniform vec3 cameraUp;
uniform vec3 cameraRight;
uniform float side;

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
 
void main() {

    vec3 center = (model * vec4(pos, 1)).xyz;
	
	vpos = center + cameraRight * ((tex.x - 0.5) * side) + cameraUp * ((tex.y - 0.5) * side); 
	vtex = tex;
	vnormal = normalize(cameraPos - center);
	
    gl_Position = projection * view * vec4(vpos, 1);    
}