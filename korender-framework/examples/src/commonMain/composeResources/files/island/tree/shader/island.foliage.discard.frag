bool pluginDiscard() {
    float d = 1.0 - abs(dot(normalize(vpos - cameraPos), normal));
    return albedo.a < max(0.12, d);
}