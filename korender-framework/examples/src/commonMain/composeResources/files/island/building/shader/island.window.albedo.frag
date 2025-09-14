vec4 pluginAlbedo() {

    vec2 f = fract(vtex);

    vec2 d = 4.0 * fwidth(vtex) * fwidth(vtex);

    float wndratio =
        smoothstep(0.2 - d.x, 0.2 + d.x,  f.x) *
        smoothstep(0.8 + d.x, 0.8 - d.x,  f.x) *
        smoothstep(0.2 - d.y, 0.2 + d.y,  f.y) *
        smoothstep(0.8 + d.y, 0.8 - d.y,  f.y);

    if (vpos.y < 130.0)
        wndratio = 0.;

    return mix(texture(baseColorTexture, vtex), vec4(0.0, 0.0, 0.0, 1.0), wndratio);
}