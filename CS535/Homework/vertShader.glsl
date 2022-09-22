#version 430
precision mediump float;
uniform float u_time;

uniform vec2 u_transVec;
uniform mat4 u_projMat;

layout (location=0) in vec2 pos;

void main(void)
{
	float c = cos(u_time);
	float s = sin(u_time);

	mat2 mRot = mat2(c, -s, s, c);

	// perform rotation matrix calculations
	vec2 updatedPos = mRot * u_transVec * pos;

	gl_Position = u_projMat * vec4(updatedPos.x, updatedPos.y, 0.0, 1.0);

}
