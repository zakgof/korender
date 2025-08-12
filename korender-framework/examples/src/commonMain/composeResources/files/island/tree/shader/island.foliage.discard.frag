bool pluginDiscard() {
    return albedo.a < max(0.15, 1.0 - dot(normalize(vpos - cameraPos), normal));
}