#version 430
precision mediump float;
uniform float u_time;

layout (location=0) in vec2 pos;

void main(void)
{
	float c = cos(u_time);
	float s = sin(u_time);

	mat2 mRot = mat2(c, -s, s, c);

	// perform rotation matrix calculations
	vec2 rotPos = mRot * pos;

	gl_Position = vec4(rotPos.x, rotPos.y, 0.0, 1.0);

}
