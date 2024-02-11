#import "header.glsl"
#import "light.glsl"
#import "texturing.glsl"

in vec3 vpos;
in vec3 vnormal;
in vec2 vtex;
#ifdef SHADOWED
in vec3 vshadow;
#endif

#ifdef ALPHA_CONTROL_V
in float valpha;
#endif

#ifndef OPAQUE
uniform sampler2D colorMap;
#endif

#ifdef SHADOWED
uniform sampler2D shadowMap;
#endif

#ifdef TRIPLANAR
uniform float triscale;
#endif

#ifdef ALPHA_CONTROL
uniform float alpha;
#endif

uniform vec3 light;
uniform vec3 cameraPos;

out vec4 fragColor;

void main()
{
	#ifdef OPAQUE
		vec4 color = vec4(1.0, 1.0, 1.0, 1.0);		
	#else
		#ifdef TRIPLANAR
			vec4 color = triplanar(colorMap, vpos * triscale, vnormal);
		#else
			vec4 color = _texture2D(colorMap, vtex);
		#endif
	#endif
	
	#ifdef PREALPHIZETEX
		color.rgb *= color.a;
	#endif
	
	#ifdef ALPHA_CONTROL
		color.a *= alpha;
	#endif
	#ifdef ALPHA_CONTROL_V
		color.a *= valpha;
	#endif
	
		
	#ifdef SOLID
		if (color.a < 0.8)
			discard;			
	#endif
	
	#ifdef FRINGES
		if (color.a > 0.8)
			discard;			
	#endif
	
	if (color.a < 0.003)
			discard;
			 
 	#ifdef SHADOWED   
		float shadow = 1.0;
		float shadowSample = _texture2D( shadowMap, vshadow.xy ).z;
		if (shadowSample  <  vshadow.z) {
		    shadow = 0.2;
		}
		color.rgb *= shadowSample;
	#endif
	    
    #ifdef SHADOW_CASTER
    	fragColor = vec4(color.a, 0.0, 0.0, 1.0);
    #endif
    
    #ifdef OUT_NORMAL
    	fragColor = vec4(vnormal * 0.5 + 0.5, color.a);
    #endif
    
    #ifdef OUT_ALBEDO
    	fragColor = color;
    #endif
    
    #ifdef OUT_DEPTH
    	fragColor = vec4(gl_FragCoord.z, 0, 0, 0);
    #endif

    
    #ifdef LIGHT
    	vec2 lt = lite(vpos, cameraPos, light, normalize(vnormal));
    	fragColor = vec4(color.rgb * (lt.x + lt.y), color.a);  	
    #endif 
    
}