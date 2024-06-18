uniform sampler2D tex1;
uniform sampler2D tex2;

vec4 pluginTexture() {
    vec4 base = texture(colorTexture, vtex);

    float sandRatio1 = clamp((vpos.y - 2.3) * 9.0 - 0.5, 0., 1.);
    float sandRatio2 = clamp((base.r - 0.5) * 8.0, 0., 1.);

    vec4 color1 = texture(tex1, vtex * 800.0f);
    vec4 color2 = texture(tex2, vtex * 1600.0f);

    vec4 detailColor = mix(color1, color2, min(sandRatio1, sandRatio2));
    return mix(detailColor, base, 0.3f);
}