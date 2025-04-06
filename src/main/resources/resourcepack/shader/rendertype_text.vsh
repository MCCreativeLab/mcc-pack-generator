#version 150

#moj_import <fog.glsl>
#moj_import <light.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec2 ScreenSize;
uniform int FogShape;

out float vertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;


#define ACTIONBAR_OFFSET 64


float get_id(float offset) {
    if (offset <= 0.0)
        return 0.0;
    return trunc(offset/1000.0);
}

void main() {
    vec3 pos = Position;

    vertexColor = Color;
    texCoord0 = UV0;
    vertexDistance = fog_distance(Position, FogShape);

    vec2 pixel = vec2(ProjMat[0][0], ProjMat[1][1]) / 2.0;
    int guiScale = int(round(pixel.x / (1 / ScreenSize.x)));
    vec2 guiSize = ScreenSize / guiScale;

    float id = get_id((round(guiSize.y - Position.y)) * -1);


    if (id > 99.0 && Color.a != 0.0) {
        float yOffset = 0.0;
        float xOffset = 0.0;
        int layer = 0;
        int type = 0;

        //if (Position.z != 0.0) {

        switch (int(id)) {

        }

        pos.y -= (id*1000) + 500;

        //pos.x -= (guiSize.x * 0.5);
        //if(Position.z == 0.0) {
        //    pos.y += (guiSize.y * 0.5);
        //}
        pos -= vec3(xOffset, yOffset, 0.0);
        pos.z += layer;
        //}
    }

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);
}
