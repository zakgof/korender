#import "!shader/lib/header.glsl"
#import "!shader/lib/sky.glsl"

in vec2 vtex;

uniform samplerCube envTexture0;

#uniforms

out vec4 fragColor;

void main() {

    vec3 look = screenToLook(vtex);

    vec3 color = texture(envTexture0, look).rgb;
    // float d = pow(max(dot(look, vec3(0., 0., -1.)), 0.), 200.);

    fragColor = vec4(color, 1.);
}