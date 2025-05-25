#version 150

uniform vec4 FillColor;
uniform vec4 OutlineColor;
uniform vec4 Roundness;
uniform vec2 QuadDimensions;
uniform float Thickness;

in vec2 texCoord0;
out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x>0.0)?r.xy : r.zw;
    r.x  = (p.y>0.0)?r.x  : r.y;
    vec2 q = abs(p)-b+r.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r.x;
}

void main() {
    vec2 p = texCoord0 * QuadDimensions - QuadDimensions * 0.5;
    float d = sdRoundBox(p, QuadDimensions * 0.5, Roundness);

    float edgeSmooth = fwidth(d);
    float inner = -Thickness;
    float fillAlpha = 1.0 - smoothstep(inner - edgeSmooth, inner + edgeSmooth, d); // Fill region
    float outlineAlpha = smoothstep(inner - edgeSmooth, inner + edgeSmooth, d) * (1.0 - smoothstep(-edgeSmooth, edgeSmooth, d)); // Outline region

    float totalAlpha = fillAlpha + outlineAlpha;
    if (totalAlpha <= 0.01) discard;

    vec4 color;
    if (FillColor.a < 0.01) {
        color = OutlineColor;
        color.a = OutlineColor.a * outlineAlpha;
    } else {
        float outlineWeight = totalAlpha > 0.0 ? outlineAlpha / totalAlpha : 0.0;
        color = mix(FillColor, OutlineColor, outlineWeight);
        color.a = FillColor.a * fillAlpha + OutlineColor.a * outlineAlpha;
    }

    fragColor = color;
}