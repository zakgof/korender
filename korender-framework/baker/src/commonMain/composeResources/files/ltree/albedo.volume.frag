uniform sampler3D volumeTexture;

vec4 pluginAlbedo() {

    vec3 start = vec3(vtex, 0.0);
    vec3 dir   = vec3(0.0, 0.0, 1.0);

    vec4 color = vec4(0.0);
    const int STEPS = 8;
    float stepSize = 1.0 / float(STEPS);

    for (int i = 0; i < STEPS; i++) {
        float t = float(i) * stepSize;
        vec3 pos = start + dir * t;
        vec4 smpl = texture(volumeTexture, pos);

        color.rgb += (1.0 - color.a) * smpl.rgb * smpl.a;
        color.a   += (1.0 - color.a) * smpl.a;

        if (color.a > 0.95) break;// early exit for opacity
    }
    return color;
}