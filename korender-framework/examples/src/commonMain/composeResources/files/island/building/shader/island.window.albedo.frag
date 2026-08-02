vec4 pluginAlbedo() {

    vec2 f = fract(vtex);

    vec2 d = 4.0 * fwidth(vtex) * fwidth(vtex);

    float wndratio =
        smoothstep(0.2 - d.x, 0.2 + d.x,  f.x) *
        smoothstep(0.8 + d.x, 0.8 - d.x,  f.x) *
        smoothstep(0.2 - d.y, 0.2 + d.y,  f.y) *
        smoothstep(0.8 + d.y, 0.8 - d.y,  f.y);

    float borderratio =
        smoothstep(0.15 - d.x, 0.15 + d.x,  f.x) *
        smoothstep(0.85 + d.x, 0.85 - d.x,  f.x) *
        smoothstep(0.15 - d.y, 0.15 + d.y,  f.y) *
        smoothstep(0.85 + d.y, 0.85 - d.y,  f.y);

    if (vpos.y < 130.0)
        wndratio = 0.;

    vec3 wall = texture(albedoTexture, vtex).rgb * (1.0  - 0.5 * borderratio);

    vec3 color = mix(wall, vec3(0.5), wndratio);
    return vec4(color, 1.0);
}