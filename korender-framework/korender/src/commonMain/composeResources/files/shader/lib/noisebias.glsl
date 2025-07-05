mat2 msw = mat2(1.6, 1.2, -1.2, 1.6);

uniform sampler2D fbmTexture;

float fbm(vec2 uv) {
    return texture(fbmTexture, uv).r;
}

float fbm2(vec2 uv, float bias) {
    return texture(fbmTexture, uv, bias).r;
}