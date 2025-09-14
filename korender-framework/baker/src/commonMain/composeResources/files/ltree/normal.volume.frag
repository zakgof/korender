uniform sampler3D volumeAlbedoTexture;
uniform sampler3D volumeNormalTexture;

vec4 volumeAlbedo = vec4(0.);

vec3 pluginNormal() {

    vec3 start = vec3(vtex, 0.0);
    vec3 dir   = vec3(0.0, 0.0, 1.0);

    vec3 volumeNormal = vec3(0.);

    const int STEPS = 16;
    float stepSize = 1.0 / float(STEPS);

    float maxAlpha = 0.0;

    for (int i = 0; i < STEPS; i++) {
        float t = float(i) * stepSize;
        vec3 pos = start + dir * t;
        vec4 smpl = texture(volumeAlbedoTexture, pos);

        volumeAlbedo.rgb += (1.0 - volumeAlbedo.a) * smpl.rgb * smpl.a;
        volumeAlbedo.a   += (1.0 - volumeAlbedo.a) * smpl.a;

        vec4 nrml = texture(volumeNormalTexture, pos);
        volumeNormal += (nrml.xyz * 2.0 - vec3(1.0)) * smpl.a;

        if (volumeAlbedo.a > 0.95) break;
    }
    return normalize(volumeNormal);
}