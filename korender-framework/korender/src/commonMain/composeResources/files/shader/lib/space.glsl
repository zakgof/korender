vec3 screenToWorldSpace(vec2 vtex, float depth) {
    vec4 ndcPosition;
    ndcPosition.xy = vtex * 2.0 - 1.0; // Convert to range [-1, 1]
    ndcPosition.z = depth * 2.0 - 1.0; // Depth range [-1, 1]
    ndcPosition.w = 1.0;
    vec4 viewPosition = inverse(projection) * ndcPosition; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    return (inverse(view) * viewPosition).xyz;
}

vec4 screenToViewSpace(vec2 vtex, float depth) {
    vec4 ndcPosition;
    ndcPosition.xy = vtex * 2.0 - 1.0; // Convert to range [-1, 1]
    ndcPosition.z = depth * 2.0 - 1.0; // Depth range [-1, 1]
    ndcPosition.w = 1.0;
    vec4 viewPosition = inverse(projection) * ndcPosition; // TODO precalc inverse as uniform
    viewPosition /= viewPosition.w;
    return viewPosition;
}