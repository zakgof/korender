#import "$vprojection"

vec3 screenToWorldSpace(vec2 vtex, float depth) {
    vec4 viewPosition = screenToViewSpace(vtex, depth);
    return (inverse(view) * viewPosition).xyz;
}

vec3 screenToLook(vec2 vtex) {
    vec3 worldPos = screenToWorldSpace(vtex, 0.5);
    return normalize(worldPos - cameraPos);
}
