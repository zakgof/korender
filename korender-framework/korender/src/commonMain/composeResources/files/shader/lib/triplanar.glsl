#ifdef TRIPLANAR

vec4 triplanarBaseColor(vec3 pos, vec3 normal) {

    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);
    vec4 blended_color;

    vec4 col1 = texture(baseColorTexture, pos.yz);
    vec4 col2 = texture(baseColorTexture, pos.zx);
    vec4 col3 = texture(baseColorTexture, pos.xy);

    return col1.xyzw * blend_weights.xxxx +
    col2.xyzw * blend_weights.yyyy +
    col3.xyzw * blend_weights.zzzz;
}

#ifdef NORMAL_MAP

vec4 triplanarNormal(vec3 pos, vec3 normal) {

    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);
    vec4 blended_color;

    vec4 col1 = texture(normalTexture, pos.yz);
    vec4 col2 = texture(normalTexture, pos.zx);
    vec4 col3 = texture(normalTexture, pos.xy);

    return col1.xyzw * blend_weights.xxxx +
    col2.xyzw * blend_weights.yyyy +
    col3.xyzw * blend_weights.zzzz;
}


#endif

#endif


/*

vec4 aperiodic(sampler2D tilesetSampler, sampler2D indexSampler, vec2 tex) {
    const float factor = 0.0625;
    vec2 intile = fract(tex) * factor;
    vec2 indexCoords = (floor(tex) + vec2(0.5, 0.5)) / 1024.0;
    int index = int(round(texture(indexSampler, indexCoords).r * 255.0));
    int xoffset = index >> 4;
    int yoffset = index & 15;
    vec2 tilepos = vec2(xoffset, yoffset) * factor;
    vec2 derx = dFdx(tex) * factor;
    vec2 dery = dFdy(tex) * factor;
    return textureGrad(tilesetSampler, tilepos + intile, derx, dery);
}

vec4 triplanarAperiodic(sampler2D colorTexture, sampler2D aperiodicTexture, vec3 pos, vec3 normal) {
    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);
    vec4 blended_color;

    vec4 col1 = aperiodic(colorTexture, aperiodicTexture, pos.yz);
    vec4 col2 = aperiodic(colorTexture, aperiodicTexture, pos.zx);
    vec4 col3 = aperiodic(colorTexture, aperiodicTexture, pos.xy);

    return col1.xyzw * blend_weights.xxxx +
    col2.xyzw * blend_weights.yyyy +
    col3.xyzw * blend_weights.zzzz;
}

*/