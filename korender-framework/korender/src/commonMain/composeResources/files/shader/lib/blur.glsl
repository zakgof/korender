vec3 blur(sampler2D tex, vec2 vtex, float radius, vec2 dir, float size) {
    const float gaussian[5] = float[](0.0614, 0.2448, 0.3877, 0.2448, 0.0614);
    vec3 color = vec3(0.0);
    vec2 step = dir * (radius / size);
    for (int i = -2; i <= 2; ++i) {
        color += texture(tex, vtex + step * float(i)).rgb * gaussian[i + 2];
    }
    return color;
}