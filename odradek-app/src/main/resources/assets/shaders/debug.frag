#version 330 core

in vec3 io_color;

out vec4 out_color;

void main() {
    out_color = vec4(io_color, 1.0);
}
