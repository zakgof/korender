vec4 pluginAlbedo(vec2 tex, vec3 pos, vec3 normal, vec4 albedo) {

    vec3 snow = vec3(0.9, 0.9, 0.9);
    vec3 rock = vec3(0.5, 0.4, 0.4);
    vec3 grass = vec3(0.2, 0.6, 0.3);


    float snowW = smoothstep(200.0, 201.0, pos.y + 300.0 * fbm2(tex));
    float rockW = smoothstep(-0.85, -0.84, -normal.y);
    float grassW = fbm2(tex * 4.0);

    vec3 color = (snow * snowW +
                 rock * rockW +
                 grass * grassW) / (snowW + rockW + grassW);


    float c = 0.25 +
              fbm2(tex *  64.0) * 0.5 +
              fbm2(tex * 128.0) * 0.25;

    return vec4(color + c, 1.0);

}