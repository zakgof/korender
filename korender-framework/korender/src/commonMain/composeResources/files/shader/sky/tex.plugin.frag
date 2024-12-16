uniform sampler2D skyTexture;

vec3 sky(vec3 look) {
    vec2 uv = skydiskfrom(look, 2.5);
    return texture(skyTexture, uv).rgb;
}