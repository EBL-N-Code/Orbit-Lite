#version 150

in vec2 texCoord0;
in vec2 dimentions;
in vec4 roundness;
in vec4 fillColor;
in vec4 borderColor;
in float borderThickness;

out vec4 fragColor;

float sdRoundBox(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x>0.0)?r.xy : r.zw;
    r.x  = (p.y>0.0)?r.x  : r.y;
    vec2 q = abs(p)-b+r.x;
    return min(max(q.x,q.y),0.0) + length(max(q,0.0)) - r.x;
}

void main() {
    vec2 halfWidthHeight = dimentions * 0.5;
    vec2 p = texCoord0 * dimentions - halfWidthHeight;
    float d = sdRoundBox(p, halfWidthHeight, roundness);

    float edgeSmooth = fwidth(d);
    float inner = -borderThickness;
    float fillAlpha = 1.0 - smoothstep(inner - edgeSmooth, inner + edgeSmooth, d); // Fill region
    float borderAlpha = smoothstep(inner - edgeSmooth, inner + edgeSmooth, d) * (1.0 - smoothstep(-edgeSmooth, edgeSmooth, d)); // Border region

    float totalAlpha = fillAlpha + borderAlpha;
    if (totalAlpha <= 0.01) discard;

    vec4 color;
    if (fillColor.a < 0.01) {
        color = borderColor;
        color.a = borderColor.a * borderAlpha;
    } else {
        float borderWeight = totalAlpha > 0.0 ? borderAlpha / totalAlpha : 0.0;
        color = mix(fillColor, borderColor, borderWeight);
        color.a = fillColor.a * fillAlpha + borderColor.a * borderAlpha;
    }

    fragColor = color;
}