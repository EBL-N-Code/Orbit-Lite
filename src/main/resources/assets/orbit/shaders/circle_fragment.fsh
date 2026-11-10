#version 150

in vec2 texCoord0;
in vec4 fillColor;
in vec4 borderColor;
in float borderThickness;

out vec4 fragColor;

void main() {
    vec2 dist = texCoord0 - vec2(0.5);
    float d = length(dist);

    const float radius = 0.5;
    float inner = radius - borderThickness;
    float edgeSmooth = fwidth(d);

    float fillAlpha = 1.0 - smoothstep(inner - edgeSmooth, inner + edgeSmooth, d); // Fill region
    float outlineAlpha = smoothstep(inner - edgeSmooth, inner + edgeSmooth, d) * (1.0 - smoothstep(radius - edgeSmooth * 2.0, radius, d)); // Outline region

    float totalAlpha = fillAlpha + outlineAlpha;
    if (totalAlpha <= 0.02) discard;

    // Use borderColor directly if fillColor is fully transparent
    vec4 color;
    if (fillColor.a < 0.01) {
        color = borderColor;
        color.a = borderColor.a * outlineAlpha;
    } else {
        float outlineWeight = totalAlpha > 0.0 ? outlineAlpha / totalAlpha : 0.0;
        color = mix(fillColor, borderColor, outlineWeight);
        color.a = max(fillColor.a * fillAlpha, borderColor.a * outlineAlpha);
    }

    fragColor = color;
}