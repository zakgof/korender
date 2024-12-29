#import "!shader/lib/header.glsl"

#define FXAA_REDUCE_MIN   1.0/128.0
#define FXAA_REDUCE_MUL   1.0/8.0
#define FXAA_SPAN_MAX     8.0

in vec2 vtex;

uniform sampler2D filterColorTexture;
uniform float screenWidth;
uniform float screenHeight;

out vec4 fragColor;

void main() {

    vec2 rcpFrame = vec2(1. / screenWidth, 1. / screenHeight);

    vec3 rgbNW = texture(filterColorTexture, vtex + (vec2(-1.0, -1.0) * rcpFrame)).xyz;
    vec3 rgbNE = texture(filterColorTexture, vtex + (vec2(1.0, -1.0) * rcpFrame)).xyz;
    vec3 rgbSW = texture(filterColorTexture, vtex + (vec2(-1.0, 1.0) * rcpFrame)).xyz;
    vec3 rgbSE = texture(filterColorTexture, vtex + (vec2(1.0, 1.0) * rcpFrame)).xyz;
    vec3 rgbM  = texture(filterColorTexture, vtex).xyz;
    vec3 luma  = vec3(0.299, 0.587, 0.114);
    
    float lumaNW = dot(rgbNW, luma);
    float lumaNE = dot(rgbNE, luma);
    float lumaSW = dot(rgbSW, luma);
    float lumaSE = dot(rgbSE, luma);
    float lumaM  = dot(rgbM,  luma);
    
    float lumaMin = min(lumaM, min(min(lumaNW, lumaNE), min(lumaSW, lumaSE)));
    float lumaMax = max(lumaM, max(max(lumaNW, lumaNE), max(lumaSW, lumaSE)));
    
    vec2 dir;
    dir.x = -((lumaNW + lumaNE) - (lumaSW + lumaSE));
    dir.y =  ((lumaNW + lumaSW) - (lumaNE + lumaSE));

    float dirReduce = max((lumaNW + lumaNE + lumaSW + lumaSE) * (0.25 * FXAA_REDUCE_MUL), FXAA_REDUCE_MIN);
    
    float rcpDirMin = 1. / (min(abs(dir.x), abs(dir.y)) + dirReduce);
    dir = min(vec2(FXAA_SPAN_MAX,  FXAA_SPAN_MAX),
              max(vec2(-FXAA_SPAN_MAX, -FXAA_SPAN_MAX),
              dir * rcpDirMin)) * rcpFrame;
    
    vec3 rgbA = 0.5 * (
        texture(filterColorTexture, vtex.xy + dir * (1./3. - 0.5)).xyz +
        texture(filterColorTexture, vtex.xy + dir * (2./3. - 0.5)).xyz);
    vec3 rgbB = rgbA * 0.5 + 0.25 * (
        texture(filterColorTexture, vtex.xy + dir * -0.5).xyz +
        texture(filterColorTexture, vtex.xy + dir * 0.5).xyz );

    float lumaB = dot(rgbB, luma);
    
    vec3 color = ((lumaB < lumaMin) || (lumaB > lumaMax)) ? rgbA : rgbB;
    fragColor = vec4(color, 1.0);
}