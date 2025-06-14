#import "!shader/lib/header.glsl"

in vec3 vpos;

uniform sampler2D depthTexture;

//////////

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
uniform vec3 cameraPos;
uniform vec2 renderSize;
uniform sampler2D decalTexture;

layout(location = 0) out vec4 decalAlbedo;
layout(location = 1) out vec4 decalNormal;

#import "!shader/lib/space.glsl"

void main() {

    vec2 uv = gl_FragCoord.xy / renderSize;
    float depth = texture(depthTexture, uv).r;
    vec3 vpos = screenToWorldSpace(uv, depth);

    vec3 decalSpacePos = (inverse(model) * vec4(vpos, 1.0)).xyz + 0.5;
    if (decalSpacePos.x < 0. || decalSpacePos.x > 1. || decalSpacePos.y < 0. || decalSpacePos.y > 1.)
        discard;

    decalAlbedo = texture(decalTexture, decalSpacePos.xy);
    decalNormal = vec4(0.);
}