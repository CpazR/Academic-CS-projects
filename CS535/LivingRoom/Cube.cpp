#include "Cube.h"


Cube::Cube(void) {
}

Cube::Cube(GLdouble size) {
	GLfloat vertices[] = {
		// Front face
		-0.5f, 0.5f, 0.5f,
		-0.5f, -0.5f, 0.5f,
		0.5f, -0.5f, 0.5f,
		0.5f, 0.5f, 0.5f,
		// Right face
		0.5f, 0.5f, 0.5f,
		0.5f, -0.5f, 0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f, 0.5f, -0.5f,
		// Back face
		0.5f, 0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f,
		-0.5f, 0.5f, -0.5f,
		// Left face
		-0.5f, 0.5f, -0.5f,
		-0.5f, -0.5f, -0.5f,
		-0.5f, -0.5f, 0.5f,
		-0.5f, 0.5f, 0.5f,
		// Top face
		-0.5f, 0.5f, -0.5f,
		-0.5f, 0.5f, 0.5f,
		0.5f, 0.5f, 0.5f,
		0.5f, 0.5f, -0.5f,
		// Bottom face
		-0.5f, -0.5f, 0.5f,
		-0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, -0.5f,
		0.5f, -0.5f, 0.5f,
	};

	GLfloat normals[] = {
		// Front
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,
		0, 0, 1,
		// Right
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,
		1, 0, 0,
		// Back
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,
		0, 0, -1,
		// Left
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,
		-1, 0, 0,
		// Top
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
		0, 1, 0,
		// Bottom
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,
		0, -1, 0,
	};

	GLushort indices[] = {
		0, 1, 2, 3, // Front
		4, 5, 6, 7, // Right
		8, 9, 10, 11, // Back
		12, 13, 14, 15, // Left
		16, 17, 18, 19, // Top
		20, 21, 22, 23, // Bottom
	};

	// Copy the vertices, normals, and indices to the tables.
	mVertices = new GLfloat[NUM_VERTICES * NUM_COORDS];
	mNormals = new GLfloat[NUM_VERTICES * NUM_COORDS];
	mIndices = new GLushort[NUM_VERTICES];
	for (int i = 0; i < NUM_VERTICES * NUM_COORDS; i += 3) {
		mVertices[i] = vertices[i];
		mVertices[i + 1] = vertices[i + 1];
		mVertices[i + 2] = vertices[i + 2];
		mNormals[i] = normals[i];
		mNormals[i + 1] = normals[i + 1];
		mNormals[i + 2] = normals[i + 2];
		mIndices[i / 3] = indices[i / 3];
	}

	mSize = size;

	polygonInit(NUM_VERTICES);
}

void Cube::render() {
	Shape::render(NUM_VERTICES);
}