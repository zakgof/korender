vec3 screenToWorldSpace(vec2 vtex, float depth) {
    vec4 ndc = vec4(vtex * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPosition = inverse(projection) * ndc; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    return (inverse(view) * viewPosition).xyz;
}

vec4 screenToViewSpace(vec2 vtex, float depth) {
    vec4 ndc = vec4(vtex * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPosition = inverse(projection) * ndc; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    return viewPosition;
}

vec3 screenToLook(vec2 vtex) {
    vec4 direction = inverse(projection * view) * vec4(vtex * 2.0 - 1.0, 0.0, 1.0); // TODO precalc inverse as uniform
    return normalize(direction.xyz / direction.w - cameraPos);
}