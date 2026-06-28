#uniform float triplanarScale;

#ifdef TRIPLANAR
    in vec3 vtriplanarpos;
    in vec3 vtriplanarnormal;

    vec4 triplanar(vec3 p, vec3 n) {

        vec3 blend_weights = abs(n.xyz);
        blend_weights = max(blend_weights, 0.0);
        blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);

        vec4 col1 = pluginTextureSource(p.yz);
        vec4 col2 = pluginTextureSource(p.zx);
        vec4 col3 = pluginTextureSource(p.xy);

        return col1.xyzw * blend_weights.xxxx +
        col2.xyzw * blend_weights.yyyy +
        col3.xyzw * blend_weights.zzzz;
    }
#endif

vec4 pluginTexturing() {
    #ifdef TRIPLANAR
        return triplanar(vtriplanarpos * triplanarScale, normalize(vtriplanarnormal));
    #else
        return vec4(1.);
    #endif
}