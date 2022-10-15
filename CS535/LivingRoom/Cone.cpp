#include "Cone.h"


Cone::Cone(void)
{
}

Cone::Cone(GLdouble base, GLdouble height, GLint slices, GLint stacks) {
	float PI = 3.14159;
	float angleStep = 2.0 * PI / (float)slices;
	int iPivot = 0;
	float slopeStep = height / (float)stacks;
	float radiusStep = base / (float)stacks;
	int numVertices = slices * (stacks + 1) * 3 + slices * 3 + 3;
	mNumIndices = slices * stacks * 6 + slices * 3; // triangles on the sides + triangles on the base
	mVertices = new GLfloat[numVertices];
	mNormals = new GLfloat[numVertices];
	mIndices = new GLushort[mNumIndices];
	float epsilon = 0.2;
	float z = 0;
	int index = 0;

	// Add the vertices and normals for the sides.
	for (int rows = 0; rows < (stacks + 1); rows++) {
		for (float angle = 0.0; angle < (2.0 * PI - epsilon); angle += angleStep) {
			// Calculate x and y position of the vertex
			GLfloat x = base * cos(angle);
			GLfloat y = base * sin(angle);
			// Calculate the normals of the vertex.
			Vector3f n(x * height / base, y * height / base, base / height);
			n.normalize();
			// Populate the arrays.
			mVertices[index] = x;
			mNormals[index++] = n.x;
			mVertices[index] = y;
			mNormals[index++] = n.y;
			mVertices[index] = z;
			mNormals[index++] = n.z;
		}
		z += slopeStep;
		base -= radiusStep;
	}

	// Add the vertices and normals for the base.
	int segmentLength = slices * NUM_COORDS;
	for (int i = 0; i < segmentLength; i += 3) {
		GLfloat x = mVertices[i];
		GLfloat y = mVertices[i + 1];
		GLfloat z = mVertices[i + 2];
		mVertices[index] = x;
		mNormals[index++] = 0;
		mVertices[index] = y;
		mNormals[index++] = 0;
		mVertices[index] = z;
		mNormals[index++] = -1.0f;
	}
	// Center point of base.
	mVertices[index] = 0;
	mNormals[index++] = 0;
	mVertices[index] = 0;
	mNormals[index++] = 0;
	mVertices[index] = 0;
	mNormals[index++] = -1;

	// Calculate the indices for the triangles that make up the sides.
	index = 0;
	for (int rows = 0; rows < stacks; rows++) {
		float currentZ = mVertices[rows * slices * 3 + 2];
		for (int i = 0; i < slices; i++) {
			int botLeft = rows * slices + i;
			int topLeft = (rows + 1) * slices + i;
			int botRight = botLeft + 1;
			int topRight = topLeft + 1;
			if (i == slices - 1) {
				botRight = rows * slices;
				topRight = (rows + 1) * slices;
			}
			// Triangle 1
			mIndices[index++] = topLeft;
			mIndices[index++] = botLeft;
			mIndices[index++] = botRight;
			// Triangle 2
			mIndices[index++] = topLeft;
			mIndices[index++] = botRight;
			mIndices[index++] = topRight;
		}
	}

	// Calculate the triangle faces for the base.
	int centerIndex = slices * stacks + slices;
	int baseIndex = slices * stacks + 1;
	for (int i = 0; i < slices; i++) {
		int botLeft = baseIndex + i;
		int botRight = botLeft + 1;
		if (i == slices - 1) {
			botRight = baseIndex;
		}
		mIndices[index++] = centerIndex;
		mIndices[index++] = botLeft;
		mIndices[index++] = botRight;
	}

	polygonInit();
}

void Cone::render() {
	//glMatrixMode(GL_MODELVIEW);
	//glPushMatrix();

	Shape::render();

	// Set array pointers
	//glEnableClientState(GL_VERTEX_ARRAY);
	//glEnableClientState(GL_NORMAL_ARRAY);
	//glVertexPointer(3, GL_FLOAT, 0, mVertices);
	//glNormalPointer(GL_FLOAT, 0, mNormals);

	//glDrawElements(GL_TRIANGLES, mNumIndices, GL_UNSIGNED_SHORT, mIndices);

	//glDisableClientState(GL_VERTEX_ARRAY);
	//glDisableClientState(GL_NORMAL_ARRAY);

	//glPopMatrix();
}