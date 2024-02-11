#import "header.glsl"

in vec3 pos;
in vec3 normal;
in vec2 tex;

uniform mat4 model; 
uniform mat4 view;
uniform mat4 projection;

#ifdef SHADOWED
uniform mat4 shadowView;
uniform mat4 shadowProjection;
#endif

out vec3 vpos;
out vec3 vnormal;
out vec2 vtex;
#ifdef SHADOWED
out vec3 vshadow;
#endif

#ifdef SHADOWED
mat4 biasMatrix = mat4(
0.5, 0.0, 0.0, 0.0,
0.0, 0.5, 0.0, 0.0,
0.0, 0.0, 0.5, 0.0,
0.5, 0.5, 0.5, 1.0
);
#endif 
 
void main() {
    vec4 p = model * vec4(pos, 1.0); 
    
    vpos = p.xyz;
    vnormal = mat3(model) * normal;    
    vtex = tex;
 
    #ifdef SHADOWED
	vshadow = (biasMatrix * shadowProjection * shadowView * p).xyz;
	#endif
    
    gl_Position = projection * view * p;
}