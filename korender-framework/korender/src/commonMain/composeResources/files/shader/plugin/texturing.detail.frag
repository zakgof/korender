uniform sampler2D detailTexture;
#uniform float detailStrength;
#uniform float detailScale;

vec4 pluginTexturing() {
    vec4 baseColor = pluginTextureSource(vtex);
    vec4 detailColor = texture(detailTexture, vtex * detailScale);
    vec3 blended = baseColor.rgb * (1.0 + (detailColor.rgb - 0.5) * 2.0 * detailStrength);
    return vec4(blended, baseColor.a);
}
