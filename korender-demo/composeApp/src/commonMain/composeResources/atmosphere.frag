#import "header.glsl"

in vec2 vtex;
uniform sampler2D filterColorTexture;
uniform sampler2D filterDepthTexture;
uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;
uniform vec3 light;

out vec4 fragColor;

void main() {

    vec4 sunProj = (projection * view * vec4(-light, 0.));
    vec2 sunPos = (projection * view * vec4(-light, 0.)).xy; // TODO vertex shader or even CPU uniform

    vec3 color = texture2D(filterColorTexture, vtex).rgb;

    vec2 csp = vec2(vtex * 2.0 - 1.0);
    vec4 direction = inverse(projection * view) * vec4(csp, 0.0, 1.0);
    vec3 look = normalize(direction.xyz / direction.w - cameraPos);

    int samples = 6;

    float depth = texture2D(filterDepthTexture, vtex).r;
    float depthFactor = clamp((depth - 0.993) / (1.0 - 0.993), 0.0, 1.0);
    float hazeRatio = 0.0;

    if (sunProj.z > 0 && sunPos.x > -1.1 && sunPos.x < 1.1 && sunPos.y > -1.1 && sunPos.y < 1.1) {
        float averageSkyLumi = 0.;
        int visibles = 0;
        for (int i = 0; i < samples; i++) {
            float phi = 2 * 3.1416 * i / float(samples);
            vec2 pt = sunPos + 0.08 * vec2(cos(phi), sin(phi));
            vec2 texpt = (pt + 1.0) * 0.5;
            if (texture2D(filterDepthTexture, texpt).r > 0.999) {
                visibles += 1;
                vec4 m = texture2D(filterColorTexture, texpt);
                averageSkyLumi += (m.r+m.g+m.b);
            }
        }
        averageSkyLumi = clamp(averageSkyLumi / float(visibles), 0.0, 3.0);
        float visibleSunRatio = float(visibles) / float(samples);

        vec2 keypt = sunPos + 0.08 * normalize(csp - sunPos);
        if (texture2D(filterDepthTexture, (keypt + 1.0) * 0.5).r > 0.999) {
            vec4 m = texture2D(filterColorTexture, (keypt + 1.0) * 0.5);
            averageSkyLumi = (m.r+m.g+m.b);
        }
        visibleSunRatio *= averageSkyLumi * 0.33;
        hazeRatio = clamp(4.0 * visibleSunRatio * clamp(0.2 / length(csp - sunPos), 0.0, 1.0), 0.0, 1.0);
    }
    if (depth > 0.999) {
        depthFactor = 0.0;
    }
    vec3 fogColor = mix(vec3(0.6, 0.6, 0.8), vec3(1.6, 1.4, 1.0), hazeRatio);
    color = mix(color, fogColor, depthFactor);

    fragColor = vec4(color, 1.0);
}