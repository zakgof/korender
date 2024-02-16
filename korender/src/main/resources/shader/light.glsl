mat3 cotangent_frame( vec3 N, vec3 p, vec2 uv ) {
	vec3 dp1 = dFdx( p );
	vec3 dp2 = dFdy( p );
	vec2 duv1 = dFdx( uv );
	vec2 duv2 = dFdy( uv );

	// solve the linear system
	vec3 dp2perp = cross( dp2, N );
	vec3 dp1perp = cross( N, dp1 );
	vec3 T = dp2perp * duv1.x + dp1perp * duv2.x;
	vec3 B = dp2perp * duv1.y + dp1perp * duv2.y;

	// construct a scale-invariant frame
	float invmax = inversesqrt( max( dot(T,T), dot(B,B) ) );
	return mat3( T * invmax, B * invmax, N );
}

vec3 perturbNormal(vec3 normal, sampler2D normalMap, vec2 uv, vec3 vPos) {

	vec3 map = texture2D( normalMap, uv ).xyz * 255./127. - 128./127.;

	mat3 TBN = cotangent_frame(normal, -vPos, uv);
	return normalize( TBN * map );

}

/*
vec2 lite(vec3 vpos, vec3 cameraPos, vec3 light, vec3 n) {  
	vec3 look = normalize(vpos - cameraPos);
	float ambient = 0.1;
	float diffuse = 0.9 * clamp(dot(-light, n), 0.0, 1.0);	
	float spec = 0.2 * pow(clamp(dot(reflect(-light, n), look), 0.0, 1.0), 6.0);	    
	return vec2(ambient, diffuse + spec);   
}


float shadow(sampler2D shadowMap, mat4 shadowMatrix, vec3 vpos) {
	vec4 proj = shadowMatrix * vec4(vpos, 1.0);
	
	vec2 off0 = 0.001 * vec2(rand(vpos.xz), rand(vpos.xz + 1.0));
	vec2 off1 = 0.0008 * vec2(rand(vpos.xz*0.43), rand(vpos.xz*0.53 + 1.0));
	vec2 off2 = 0.0005 * vec2(rand(vpos.xz*0.98), rand(vpos.xz*0.67 + 1.0));
	
	return 1.0 
	- 0.4 * _texture2D(shadowMap, proj.xy * 0.5 + vec2(0.5, 0.5) + off0).r
	- 0.3 * _texture2D(shadowMap, proj.xy * 0.5 + off1 + vec2(0.5, 0.5), 1.0).r
	- 0.3 * _texture2D(shadowMap, proj.xy * 0.5 + off2 + vec2(0.5, 0.5), 2.0).r;
}
*/