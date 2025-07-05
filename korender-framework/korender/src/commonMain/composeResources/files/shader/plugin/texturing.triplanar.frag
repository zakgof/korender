#uniform float triplanarScale;

vec4 triplanar(sampler2D tex, vec3 pos, vec3 normal) {

    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);
    vec4 blended_color;

    vec4 col1 = texture(tex, pos.yz);
    vec4 col2 = texture(tex, pos.zx);
    vec4 col3 = texture(tex, pos.xy);

    return col1.xyzw * blend_weights.xxxx +
    col2.xyzw * blend_weights.yyyy +
    col3.xyzw * blend_weights.zzzz;
}

vec4 pluginTexturing() {
    return triplanar(baseColorTexture, vpos * triplanarScale, vnormal);
}