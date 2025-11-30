#import "$vprojection"

float pluginDepth() {
    vec4 vclip = pluginVProjection((view * vec4(position, 1.0)).xyz);
    return 0.5 * vclip.z / vclip.w + 0.5;
}