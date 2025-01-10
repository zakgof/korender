uniform float time;

vec4 pluginTexture(vec4 albedo) {
    vec2 uv = 0.05 * floor(vpos.xy) + 0.13 * floor(vpos.zy);

    // TODO extract as golden noise
    float PHI = 1.61803398874989484820459;
    float noise = fract(tan(distance(uv*PHI, uv)*32.0f)*(uv.x));

    float discriminator = step(sin(noise * (100.0f + time * 0.01)), -0.93);


    // TODO optimize
    vec2 tx = fract(vtex);
    if (tx.x < 0.1 || tx.x > 0.9 || tx.y < 0.1 || tx.y > 0.9)
        discriminator = 0.5;

    vec2 dx = dFdx(vtex);
    vec2 dy = dFdy(vtex);
    float smth = clamp((dot(dx,dx) + dot(dy,dy)) * 1.0, 0., 1.);
    discriminator = mix(discriminator, 0.3, smth);

    return vec4(discriminator, discriminator, discriminator, 1.0) * albedo;
}