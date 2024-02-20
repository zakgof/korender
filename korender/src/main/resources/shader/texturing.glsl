float noi(sampler2D noisetex, vec2 pos, float sz, float scl) {
    vec4 samp = texture2D(noisetex, pos * sz);
    return (samp.r - 0.5) * scl;
}

float fbm(sampler2D noisetex, vec2 pos) {
    float noize = 0.0;
    noize += noi(noisetex, pos, 3.9, 0.512);
    noize += noi(noisetex, pos, 9.21, 0.18);
    noize += noi(noisetex, pos, 19.21, 0.1);
    return noize;
}


vec4 hash4(vec2 p) { return fract(sin(vec4(1.0 + dot(p, vec2(37.0, 17.0)),
                                           2.0 + dot(p, vec2(11.0, 47.0)),
                                           3.0 + dot(p, vec2(41.0, 29.0)),
                                           4.0 + dot(p, vec2(23.0, 31.0)))) * 103.0); }

vec4 randomizedTexture2D(sampler2D tex, vec2 uv) {

    vec2 p = floor(uv);
    vec2 f = fract(uv);

    // derivatives (for correct mipmapping)
    vec2 ddx = dFdx(uv);
    vec2 ddy = dFdy(uv);

    // voronoi contribution
    vec4 va = vec4(0.0);
    float wt = 0.0;
    for (int j = -1; j <= 1; j++)
    for (int i = -1; i <= 1; i++)
    {
        vec2 g = vec2(float(i), float(j));
        vec4 o = hash4(p + g);
        vec2 r = g - f + o.xy;
        float d = dot(r, r);
        float w = exp(-5.0 * d);
        vec4 c = textureGrad(tex, uv + o.zw, ddx, ddy);
        va += w * c;
        wt += w;
    }

    // normalization
    return va / wt;
}


vec4 triplanar(sampler2D tex, vec3 pos, vec3 normal) {
    vec3 blend_weights = abs(normal.xyz);
    blend_weights = max(blend_weights, 0.0);
    blend_weights /= (blend_weights.x + blend_weights.y + blend_weights.z);
    vec4 blended_color;

    vec4 col1 = texture2D(tex, pos.yz);
    vec4 col2 = texture2D(tex, pos.zx);
    vec4 col3 = texture2D(tex, pos.xy);

    return col1.xyzw * blend_weights.xxxx +
      col2.xyzw * blend_weights.yyyy +
      col3.xyzw * blend_weights.zzzz;
}

vec4 aperiodic(sampler2D tilesetSampler, sampler2D indexSampler, vec2 tex) {
    const float factor = 0.0625;
    vec2 intile = fract(tex) * factor;
    vec2 indexCoords = (floor(tex) + vec2(0.5, 0.5)) / 1024.0;
    int index = int(round(texture2D(indexSampler, indexCoords).r * 255.0));
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