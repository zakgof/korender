#import "!shader/lib/header.glsl"
#import "!shader/lib/sky.glsl"

in vec2 vtex;

uniform sampler2D filterColorTexture;
uniform sampler2D envTextureTop0;
uniform sampler2D envTextureBottom0;

uniform mat4 projection;
uniform mat4 view;
uniform vec3 cameraPos;

out vec4 fragColor;

void main() {

    vec3 look = screentolook(vtex, projection * view, cameraPos);

    vec2 uvTop    = skydiskfromlook(look, 1.5);
    vec2 uvBottom = skydiskfromlook(vec3(look.x, -look.y, look.z), 1.5);

    float ratio = smoothstep(-0.001, 0.001, look.y);
    vec3 color = mix(texture(envTextureBottom0, uvBottom).rgb, texture(envTextureTop0, uvTop).rgb, ratio);

    // color = texture(envTextureTop0, vtex).rgb;
    // float d = pow(max(dot(look, vec3(0., 0., -1.)), 0.), 200.);

    fragColor = vec4(color, 1.);
}