#pragma once
#include "shape.h"
class Sphere :
	public Shape
{
public:
	Sphere(void);
	Sphere(GLdouble radius, GLint slices, GLint stacks);
	void render(void);

private:
	int mNumIndices;
	GLdouble mSize;
};
