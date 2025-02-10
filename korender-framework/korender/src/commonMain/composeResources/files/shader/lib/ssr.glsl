uniform samplerCube envTexture;

vec3 ssr(vec3 vpos, vec3 N, vec3 V) {
    vec3 rayDir = normalize(reflect(-V, N));
    vec3 rayPoint = vpos;
    vec3 step = rayDir * 0.05;

    for (int i = 0; i < 50; i++) {
        rayPoint += step;

        vec4 p = projection * view * vec4(rayPoint, 1.0);
        p.xyz /= p.w;

        vec3 uv = vec3(0.5) + p.xyz * 0.5;

        if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0)
            break;

        float d = texture(depthTexture, uv.xy).r;

        if (d - uv.z < 0.00)
            return texture(cdiffTexture, uv.xy).rgb;
    }

    return texture(envTexture, rayDir).rgb;
}