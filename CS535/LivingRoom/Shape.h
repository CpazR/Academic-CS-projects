#pragma once
#include <GL/glut.h>
#include <GL/gl.h>
#include "Vector3f.h"

/**
* Defines a parent class for the primitives.
* Megan Worley
*/
class Shape
{
public:
	Shape(void);
	~Shape(void);
	virtual void render();

protected:
	GLfloat* mVertices;
	GLushort* mIndices;
	GLfloat* mNormals;
	static const int NUM_COORDS = 3;
};
