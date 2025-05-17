#version 330 core

in vec2 in_position;
in vec2 in_uv;
in vec3 in_color;

out vec2 io_uv;
out vec3 io_color;

uniform mat4 u_transform;

void main() {
    gl_Position = u_transform * vec4(in_position, 0.0, 1.0);
    io_uv = in_uv;
    io_color = in_color;
}
