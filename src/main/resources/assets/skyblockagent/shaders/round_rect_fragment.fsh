#version 150

uniform vec4 ColorModulator;
uniform float Roundness;
uniform vec2 QuadDimensions;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - b + r;
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r;
}

void main() {
    vec2 p = texCoord0 * QuadDimensions - QuadDimensions * 0.5;
    float d = sdRoundBox(p, QuadDimensions * 0.5, Roundness);
    float alpha = 1.0 - smoothstep(-fwidth(d), fwidth(d), d);
    vec4 color = ColorModulator * vertexColor * vec4(1, 1, 1, alpha);
    if (color.a == 0.0) discard;
    fragColor = color;
}