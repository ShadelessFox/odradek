#version 450 core

in vec4 in_position_size;
in vec3 in_color;

out vec3 io_color;

uniform mat4 u_view;
uniform mat4 u_projection;

void main() {
    gl_Position = u_projection * u_view * vec4(in_position_size.rgb, 1.0);
    gl_PointSize = in_position_size.w;
    io_color = in_color;
}
