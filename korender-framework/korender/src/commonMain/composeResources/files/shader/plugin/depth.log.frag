#import "$vprojection"

float pluginDepth() {
    return log2(vdepth) / log2(projectionFar + 1.0);
}