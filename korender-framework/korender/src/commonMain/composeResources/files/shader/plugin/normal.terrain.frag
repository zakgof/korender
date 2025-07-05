#ifdef PLUGIN_TERRAIN
    #import "$terrain"
#endif

vec3 pluginNormal() {

    float nstep = 1.0 / float(pluginTerrainTextureSize());

    float hL = pluginTerrainHeight(vtex + vec2(-nstep, 0.));
    float hR = pluginTerrainHeight(vtex + vec2(nstep, 0.));
    float hD = pluginTerrainHeight(vtex + vec2(0, -nstep));
    float hU = pluginTerrainHeight(vtex + vec2(0, nstep));

    vec3 dx = normalize(vec3(2.0,  hR - hL, 0.0));
    vec3 dz = normalize(vec3(0.0,  hU - hD, 2.0));

    return normalize(cross(dz, dx));
}