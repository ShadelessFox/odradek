#version 330 core
        
in vec3 in_position;
in vec4 in_color_point_size;

out vec3 io_color;

uniform mat4 u_view;
uniform mat4 u_projection;

void main() {
    gl_Position = u_projection * u_view * vec4(in_position, 1.0);
    gl_PointSize = in_color_point_size.w;
    io_color = in_color_point_size.rgb;
}
