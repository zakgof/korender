#import "!shader/lib/header.glsl"

in vec2 vtex;

uniform sampler2D cdiffTexture;
uniform sampler2D normalTexture;
uniform sampler2D materialTexture;
uniform sampler2D emissionTexture;
uniform sampler2D depthTexture;

uniform vec3 cameraPos;
uniform vec3 cameraDir;
uniform mat4 projection;
uniform mat4 view;

#ifdef SSR_ENV
uniform samplerCube envTexture;
#endif

#import "!shader/lib/space.glsl"

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

        if (d - uv.z < 0.0)
            return texture(cdiffTexture, uv.xy).rgb;
    }

    #ifdef SSR_ENV
    return texture(envTexture, rayDir).rgb;
    #else
    return vec3(0.);
    #endif

}

void main() {

    float depth = texture(depthTexture, vtex).r;
    vec3 vpos = screenToWorldSpace(vtex, depth);

    vec3 c_diff = texture(cdiffTexture, vtex).rgb;
    vec4 materialTexel = texture(materialTexture, vtex);
    vec4 emissionTexel = texture(emissionTexture, vtex);
    vec4 normalTexel = texture(normalTexture, vtex);

    vec3 F0 = materialTexel.rgb;
    float rough = materialTexel.a;

    vec3 V = normalize(cameraPos - vpos);
    vec3 N = normalize(normalTexel.rgb * 2.0 - 1.0);

    //

    vec3 reflection = ssr(vpos, N, V);
    float NdotV = max(dot(N, V), 0.0);
    vec3 FR = F0 + (1. - F0) * pow(1. - NdotV, 5.);

    gl_FragColor = vec4(reflection * FR, 1.0);
}