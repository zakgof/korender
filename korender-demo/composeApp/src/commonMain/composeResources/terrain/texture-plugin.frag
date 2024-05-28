uniform sampler2D tex1;
uniform sampler2D tex2;

vec4 pluginTexture() {
    vec4 base = texture(colorTexture, vtex);
    vec4 color1 = texture(tex1, vtex * 800.0f);
    vec4 color2 = texture(tex2, vtex * 1600.0f);
    return base.r > 0.5 ? color1 : color2;
}