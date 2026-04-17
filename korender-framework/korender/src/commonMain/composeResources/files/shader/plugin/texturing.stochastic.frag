#uniform float stochasticSharpness;

vec4 pluginTexturing() {

    const mat2 skewMat = mat2(1.0, 0.0, 0.5, 1.0);
    const mat2 unskewMat = mat2(1.0, 0.0, -0.5, 1.0);

    vec2 skewedUV = skewMat * vtex;
    vec2 baseId = floor(skewedUV);
    vec2 f = fract(skewedUV);

    vec2 temp = (f.x > f.y) ? vec2(baseId.x + 1.0, baseId.y) : vec2(baseId.x, baseId.y + 1.0);

    vec2 w1 = baseId;
    vec2 w2 = temp;
    vec2 w3 = baseId + 1.0;

    vec2 uv1 = uv + hash(w1);
    vec2 uv2 = uv + hash(w2);
    vec2 uv3 = uv + hash(w3);

    vec3 weights;
    vec2 p = unskewMat * f;
    weights.z = p.y;
    weights.x = 1.0 - p.x;
    weights.y = p.x - p.y;

    if (f.x > f.y) {
        weights = vec3(1.0 - f.x, f.x - f.y, f.y);
    } else {
        weights = vec3(1.0 - f.y, f.y - f.x, f.x);
    }

    weights = pow(weights, vec3(sharpness));
    weights /= (weights.x + weights.y + weights.z);

    vec2 dx = dFdx(uv);
    vec2 dy = dFdy(uv);

    return  pluginTextureSourceGrad(uv1, dx, dy) * weights.x +
            pluginTextureSourceGrad(uv2, dx, dy) * weights.y +
            pluginTextureSourceGrad(uv3, dx, dy) * weights.z;
}