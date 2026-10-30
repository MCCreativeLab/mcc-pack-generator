#version 330
#extension GL_ARB_separate_shader_objects : require

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
#include <minecraft:fog.glsl>
#include <minecraft:sample_lightmap.glsl>
#endif

#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
layout(location = 3) in ivec2 UV2;
#endif

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
uniform sampler2D Sampler2;
layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
#endif

layout(location = 2) out vec4 vertexColor;
layout(location = 3) out vec2 texCoord0;

void main() {
    vec3 pos = Position;
#ifdef IS_GUI
    // MCC bitmap glyphs encode their screen anchor in their ascent.
    // The projection carries the exact GUI dimensions, including fractional
    // framebuffer scaling. Globals.ScreenSize can describe another render target.
    vec2 guiSize = vec2(2.0 / ProjMat[0][0], -2.0 / ProjMat[1][1]);
    float id = trunc(-round(guiSize.y - Position.y) / 1000.0);
    if (id > 99.0 && Color.a != 0.0) {
        float xOffset = 0.0;
        float yOffset = 0.0;
        int layer = 0;
        switch (int(id)) {
        }
        pos.y -= (id * 1000.0) + 500.0;
        pos -= vec3(xOffset, yOffset, 0.0);
        pos.z += layer;
    }
#endif
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

#if !defined(IS_GUI) && !defined(IS_SEE_THROUGH)
    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
#else
    vertexColor = Color;
#endif
    texCoord0 = UV0;
}
