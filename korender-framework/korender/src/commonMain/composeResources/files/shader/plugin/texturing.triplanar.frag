#uniform float triplanarScale;

vec4 triplanar(vec3 pos, vec3 normal) {

    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);

    vec4 col1 = pluginTextureSource(pos.yz);
    vec4 col2 = pluginTextureSource(pos.zx);
    vec4 col3 = pluginTextureSource(pos.xy);

    return col1.xyzw * blend_weights.xxxx +
    col2.xyzw * blend_weights.yyyy +
    col3.xyzw * blend_weights.zzzz;
}

vec4 pluginTexturing() {
    return triplanar(position * triplanarScale, normal);
}