vec3 ssr(vec3 viewPos, vec3 N) {
    vec3 reflectDir = normalize(reflect(viewPos, N));
    vec3 rayOrigin = viewPos;
    vec3 step = reflectDir * 0.1;

    for (int i = 0; i < 50; i++) {
        rayOrigin += step;

        vec4 clipPos = projection * vec4(rayOrigin, 1.0);
        vec2 uv = (clipPos.xy / clipPos.w) * 0.5 + 0.5;

        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
        break;

        float d = texture(depthTexture, uv).r;
        vec3 scenePos = screenToViewSpace(uv, d).xyz;

        if (length(scenePos - rayOrigin) < 0.1)
        return texture(cdiffTexture, uv).rgb;
    }

    return vec3(0.0);
}