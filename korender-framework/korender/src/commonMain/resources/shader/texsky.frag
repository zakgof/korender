#import "header.glsl"
#import "sky.glsl"

in vec2 vtex;

uniform vec3 cameraPos;
uniform mat4 view;
uniform mat4 projection;
uniform sampler2D skyTexture;

out vec4 fragColor;

void main() {
    vec2 uv = skydisk(vtex, cameraPos, projection * view);
    fragColor = texture2D(skyTexture, uv);
}