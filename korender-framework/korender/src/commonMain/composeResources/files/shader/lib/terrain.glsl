#ifdef PLUGIN_TERRAIN
    #import "$terrain"
#else
    float heightAt(vec2 uv) {
        if (uv.x < 0. || uv.x > 1. || uv.y < 0. || uv.y > 1.)
            return outsideHeight;
        return heightScale * texture(heightTexture, vec2(uv.x, uv.y)).r + terrainCenter.y;
    }
#endif


vec3 normalAt(vec2 uv, float resolution) {

    float nstep = 1.0 / resolution;

    float hL = heightAt(uv + vec2(-nstep, 0.));
    float hR = heightAt(uv + vec2(nstep, 0.));
    float hD = heightAt(uv + vec2(0, -nstep));
    float hU = heightAt(uv + vec2(0, nstep));

    vec3 dx = normalize(vec3(2.0,  hR - hL, 0.0));
    vec3 dz = normalize(vec3(0.0,  hU - hD, 2.0));

    return normalize(cross(dz, dx));
}