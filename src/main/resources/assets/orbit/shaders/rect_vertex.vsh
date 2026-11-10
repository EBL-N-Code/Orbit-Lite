#version 150

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>

in vec3 Position;
in vec2 UV0;
in vec2 UV1;
in vec4 Roundness;
in vec4 Color;
in vec4 BorderColor;
in float BorderThickness;

out vec2 texCoord0;
out vec2 dimentions;
out vec4 roundness;
out vec4 fillColor;
out vec4 borderColor;
out float borderThickness;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    texCoord0 = UV0;
    dimentions = UV1;
    roundness = Roundness;
    fillColor = Color;
    borderColor = BorderColor;
    borderThickness = BorderThickness;
}