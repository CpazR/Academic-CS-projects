#version 430
uniform mat4 u_modelViewProjection;

layout (location=0) in vec2 pos;

void main(void)
{
	gl_Position = u_modelViewProjection * vec4(pos.x, pos.y, 0.0, 1.0);
}
