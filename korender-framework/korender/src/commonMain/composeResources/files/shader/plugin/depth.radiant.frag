float pluginDepth() {
    vec4 vclip = projection * (view * vec4(position, 1.0));
    return 0.5 * vclip.z / vclip.w + 0.5;
}