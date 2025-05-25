#version 150

uniform vec4 FillColor;
uniform vec4 OutlineColor;
uniform float Thickness;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 dist = texCoord0 - vec2(0.5);
    float d = length(dist);

    const float radius = 0.5;
    float inner = radius - Thickness;
    float edgeSmooth = fwidth(d);

    float fillAlpha = 1.0 - smoothstep(inner - edgeSmooth, inner + edgeSmooth, d); // Fill region
    float outlineAlpha = smoothstep(inner - edgeSmooth, inner + edgeSmooth, d) * (1.0 - smoothstep(radius - edgeSmooth * 2.0, radius, d)); // Outline region

    float totalAlpha = fillAlpha + outlineAlpha;
    if (totalAlpha <= 0.02) discard;

    // Use OutlineColor directly if FillColor is fully transparent
    vec4 color;
    if (FillColor.a < 0.01) {
        color = OutlineColor;
        color.a = OutlineColor.a * outlineAlpha;
    } else {
        float outlineWeight = totalAlpha > 0.0 ? outlineAlpha / totalAlpha : 0.0;
        color = mix(FillColor, OutlineColor, outlineWeight);
        color.a = max(FillColor.a * fillAlpha, OutlineColor.a * outlineAlpha);
    }

    fragColor = color;
}