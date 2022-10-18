#pragma once
#include <GL/glut.h>
#include <GL/gl.h>
#include <vector>
#include "Vector3f.h"

/**
* Defines a parent class for the primitives.
* Megan Worley
*/
class Shape {
	public:
		Shape(void);
		GLfloat* getMVertices();
		GLuint getVao();
		GLuint getVbo();
		virtual void render();

	protected:
		void polygonInit();
		GLfloat* mVertices;
		GLushort* mIndices;
		GLfloat* mNormals;
		static const int NUM_COORDS = 3;
		GLuint vao; // Reference to existing vao
		GLuint vbo; // Each shape has its own vbo
};
