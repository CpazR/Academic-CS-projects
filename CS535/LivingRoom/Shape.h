#pragma once
#include <GL/glut.h>
#include <GL/gl.h>
#include <vector>
#include "Vector3f.h"
#include <glm/gtc/matrix_transform.hpp>

/**
* Defines a parent class for the primitives.
* Megan Worley
*/
class Shape {
	public:
		Shape(void);
		std::vector <glm::vec3> getMVertices();
		GLuint* getVbo();

	protected:
		void polygonInit();
		std::vector<glm::vec3> mVertices;
		std::vector<GLushort> mIndices;
		std::vector<glm::vec3> mNormals;
		static const int NUM_COORDS = 3;
		GLuint vbo[3]; // Each shape has its own vbo
		virtual void render();
};
