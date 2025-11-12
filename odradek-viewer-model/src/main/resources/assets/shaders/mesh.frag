#version 450 core
#extension GL_ARB_bindless_texture : require

#define FLAG_HAS_NORMAL (1<<0)
#define FLAG_HAS_UV     (1<<1)

in vec3 io_position;
in vec3 io_normal;
in vec2 io_uv;

out vec4 out_color;

layout(bindless_sampler) uniform;
uniform vec3 u_view_position;
uniform vec3 u_color;
uniform int  u_flags;
uniform sampler2D u_texture;

void main() {
    vec3 normal;
    if ((u_flags & FLAG_HAS_NORMAL) != 0) {
        normal = normalize(io_normal);
    } else {
        normal = normalize(cross(dFdx(io_position), dFdy(io_position)));
    }

    vec3 view = normalize(u_view_position - io_position);
    vec3 color = vec3(abs(dot(view, normal))) * u_color;

    if ((u_flags & FLAG_HAS_UV) != 0)
        color *= texture(u_texture, io_uv).rgb;

    out_color = vec4(color, 1.0);
}
