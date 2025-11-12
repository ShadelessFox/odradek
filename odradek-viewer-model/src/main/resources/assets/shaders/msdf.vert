#version 450 core

in vec4 in_position_uv;
in vec3 in_color;

out vec2 io_uv;
out vec3 io_color;

uniform mat4 u_transform;

void main() {
    gl_Position = u_transform * vec4(in_position_uv.xy, 0.0, 1.0);
    io_uv = in_position_uv.zw;
    io_color = in_color;
}
