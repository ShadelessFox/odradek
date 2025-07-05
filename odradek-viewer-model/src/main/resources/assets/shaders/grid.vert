// https://asliceofrendering.com/scene%20helper/2020/01/05/InfiniteGrid/

#version 330 core

in vec2 in_position;

uniform mat4 u_view;
uniform mat4 u_projection;

out vec3 io_near_point;
out vec3 io_far_point;

vec3 unprojectPoint(float x, float y, float z) {
    mat4 viewInv = inverse(u_view);
    mat4 projInv = inverse(u_projection);
    vec4 unprojectedPoint = viewInv * projInv * vec4(x, y, z, 1.0);
    return unprojectedPoint.xyz / unprojectedPoint.w;
}

void main() {
    io_near_point = unprojectPoint(in_position.x, in_position.y, 0.0).xyz;
    io_far_point = unprojectPoint(in_position.x, in_position.y, 1.0).xyz;
    gl_Position = vec4(in_position, 0.0, 1.0);
}
