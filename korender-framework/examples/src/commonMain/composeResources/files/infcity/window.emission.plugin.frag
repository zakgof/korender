uniform sampler2D windowTexture;
uniform sampler2D noiseTexture;

vec3 pluginEmission() {

    float delta = fract(vpos.x);
    vec2 uv = delta < 0.001 || delta > 0.999 ? vpos.zy : vpos.xy;

    float noise = textureLod(noiseTexture, 0.01 * floor(uv) + vec2(time * 0.0002), 0.0).r;
    vec3 emission = texture(windowTexture, vtex).rgb;

    return emission * step(0.52, noise);
}