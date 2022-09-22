#version 430
layout (location=0) in vec2 pos;
uniform mat4 u_projMat;

void main(void)
{
	gl_Position = u_projMat * vec4(pos, 1.0);
}
