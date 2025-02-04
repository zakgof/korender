uniform float time;
uniform sampler2D windowTexture;

vec3 pluginEmission(vec3 dummy) {

    vec2 uv = 0.05 * floor(vpos.xy) + 0.13 * floor(vpos.zy);

    // TODO extract as golden noise
    float PHI = 1.61803398874989484820459;
    float noise = fract(tan(distance(uv*PHI, uv)*32.0f)*(uv.x));

    vec2 dx = dFdx(vtex);
    vec2 dy = dFdy(vtex);
    float smth = clamp((dot(dx,dx) + dot(dy,dy)) * 0.4, 0., 1.);


    float discriminator = step(0.5, sin(noise * (100.0f + time * 0.01)));
    discriminator = mix(discriminator, 0.25, smth);

    vec3 emission = textureGrad(windowTexture, fract(vtex), dx, dy).rgb;

    return emission * discriminator;
}