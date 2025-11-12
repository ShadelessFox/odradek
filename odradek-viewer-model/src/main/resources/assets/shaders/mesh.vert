#version 450 core
#define FLAG_HAS_NORMAL (1<<0)
#define FLAG_HAS_UV     (1<<1)

layout (location = 0) in vec3 in_position;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv;

out vec3 io_position;
out vec3 io_normal;
out vec2 io_uv;

uniform mat4 u_model;
uniform mat4 u_view;
uniform mat4 u_projection;
uniform int  u_flags;

void main() {
    io_position = vec3(u_model * vec4(in_position, 1.0));
    if ((u_flags & FLAG_HAS_NORMAL) != 0)
        io_normal = mat3(transpose(inverse(u_model))) * in_normal;
    if ((u_flags & FLAG_HAS_UV) != 0)
        io_uv = in_uv;

    gl_Position = u_projection * u_view * u_model * vec4(in_position, 1.0);
}
