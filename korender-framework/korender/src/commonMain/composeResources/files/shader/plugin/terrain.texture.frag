uniform sampler2D heightTexture;
#uniform float heightScale;
#uniform float outsideHeight;
#uniform vec3 terrainCenter;
#uniform int heightTextureSize;

vec3 pluginTerrainCenter() {
    return terrainCenter;
}

int pluginTerrainTextureSize() {
    return heightTextureSize;
}

float pluginTerrainHeight(vec2 uv) {
    if (uv.x < 0. || uv.x > 1. || uv.y < 0. || uv.y > 1.)
        return outsideHeight;
    return heightScale * texture(heightTexture, uv).r + terrainCenter.y;
}