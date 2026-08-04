#import "$terrain"
#uniform float cell;

#ifdef ANTITERRACE
    #uniform float antiTerraceStep;   // widened sampling step in texels (e.g. 4.0)
    #uniform float antiTerraceThreshold; // flatness blend threshold (e.g. 0.15)
#endif

float nstep = 1.0 / float(pluginTerrainTextureSize()); // UV offset for 1 texel

vec3 computeTerrainNormalAtStep(float stepTexels) {
    float uvOffset = stepTexels * nstep;   // UV offset for `stepTexels` texels
    float ws = stepTexels * cell;      // world distance for `stepTexels` texels

    float hL = pluginTerrainHeight(vtex + vec2(-uvOffset, 0.0));
    float hR = pluginTerrainHeight(vtex + vec2( uvOffset, 0.0));
    float hD = pluginTerrainHeight(vtex + vec2(0.0, -uvOffset));
    float hU = pluginTerrainHeight(vtex + vec2(0.0,  uvOffset));

    vec3 dx = vec3(2.0 * ws, hR - hL, 0.0);
    vec3 dz = vec3(0.0, hU - hD, 2.0 * ws);
    return normalize(cross(dz, dx));
}

vec3 pluginNormal() {
    vec3 nSmall = computeTerrainNormalAtStep(1.0);
    #ifdef ANTITERRACE
        vec3 nLarge = computeTerrainNormalAtStep(antiTerraceStep);
        float flatness = 1.0 - smoothstep(0.0, antiTerraceThreshold, 1.0 - nSmall.y);
        return normalize(mix(nSmall, nLarge, flatness));
    #else
        return nSmall;
    #endif
}