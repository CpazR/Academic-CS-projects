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
		std::vector <GLfloat> getMVertices();
		GLuint getVbo();

	protected:
		void polygonInit();
		std::vector<GLfloat> mVertices;
		std::vector <GLushort> mIndices;
		std::vector <GLfloat> mNormals;
		static const int NUM_COORDS = 3;
		GLuint vbo; // Each shape has its own vbo
		virtual void render();
};
