#pragma once
#include <GL/gl.h>
#include <cmath>

// Defines a 3D vector and some helper methods for normalization, magnitude, etc.
// Megan Worley
struct Vector3f {
	GLfloat x;
	GLfloat y;
	GLfloat z;

	Vector3f(GLfloat x, GLfloat y, GLfloat z) {
		this->x = x;
		this->y = y;
		this->z = z;
	}

	~Vector3f(void) {
	}

	GLfloat magnitude() {
		return sqrt(x * x + y * y + z * z);
	}

	void normalize() {
		GLfloat mag = magnitude();
		x = x / mag;
		y = y / mag;
		z = z / mag;
	}

	Vector3f getNormalized() {
		GLfloat mag = magnitude();
		Vector3f unitVector(x, y, z);
		unitVector.normalize();
		return unitVector;
	}
};
