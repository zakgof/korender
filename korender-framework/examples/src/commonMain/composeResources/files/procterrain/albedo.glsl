vec4 pluginAlbedo(vec2 tex, vec3 pos, vec3 normal, vec4 albedo) {


    float splatmap = smoothstep(300.0, 301.0, pos.y + 300.0 * fbm2(tex));

    vec3 color = mix(vec3(1.0, 0.0, 0.0), vec3(0.0, 1.0, 0.0), splatmap);


    float c = 0.25 +
              fbm2(tex *  64.0) * 0.5 +
              fbm2(tex * 128.0) * 0.25;

    return vec4(color * c, 1.0);

}