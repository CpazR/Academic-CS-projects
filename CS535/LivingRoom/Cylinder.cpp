#include "Cylinder.h"


Cylinder::Cylinder(void)
{
}

Cylinder::Cylinder(GLdouble base, GLdouble height, GLint slices, GLint stacks) {
	float PI = 3.14159;
	float angleStep = 2.0 * PI / (float)slices;
	int iPivot = 0;
	float slopeStep = height / (float)stacks;
	mNumIndices = (slices * stacks) * 6 + slices * 6;
	int numVertices = slices * (stacks + 1) + slices * 2 + 2; // Center section + 2 bases + 2 centers of each base
	// Calculate the vertices of the sides of the cylinder.
	mVertices = new GLfloat[numVertices * NUM_COORDS];
	mNormals = new GLfloat[numVertices * NUM_COORDS];
	mIndices = new GLushort[mNumIndices];
	int index = 0;
	float epsilon = 0.2f;
	GLfloat z = 0;
	for (int rows = 0; rows < stacks + 1; rows++) {
		for (float angle = 0.0; angle < (2.0 * PI - epsilon); angle += angleStep) {
			// Calculate x and y position of the next vertex
			GLfloat x = cos(angle);
			GLfloat y = sin(angle);
			Vector3f n(x, y, 0);
			n.normalize();
			mVertices[index] = x;
			mNormals[index++] = n.x;
			mVertices[index] = y;
			mNormals[index++] = n.y;
			mVertices[index] = z;
			mNormals[index++] = n.z;
		}
		z += slopeStep;
	}

	// Add the vertices and normals for the caps.
	for (int cap = 0; cap < 2; cap++) {
		int capIndex = 0;
		if (cap == 1) {
			capIndex = slices * stacks * NUM_COORDS;
		}
		for (int i = 0; i < slices * NUM_COORDS; i += 3) {
			GLfloat x = mVertices[capIndex + i];
			GLfloat y = mVertices[capIndex + i + 1];
			z = mVertices[capIndex + i + 2];
			mVertices[index] = x;
			mNormals[index++] = 0;
			mVertices[index] = y;
			mNormals[index++] = 0;
			mVertices[index] = z;
			if (cap == 0) {
				mNormals[index++] = -1.0f;
			}
			else {
				mNormals[index++] = 1.0f;
			}
		}
		// Center of cap
		mVertices[index] = 0;
		mNormals[index++] = 0;
		mVertices[index] = 0;
		mNormals[index++] = 0;
		mVertices[index] = z;
		if (cap == 0) {
			mNormals[index++] = -1.0f;
		}
		else {
			mNormals[index++] = 1.0f;
		}
	}

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

	// Add the face indices for the caps.
	for (int cap = 0; cap < 2; cap++) {
		int capIndex = slices * (stacks + 1);
		int centerIndex = slices * (stacks + 2);
		if (cap == 1) {
			capIndex = slices * (stacks + 2) + 1;
			centerIndex = slices * (stacks + 3) + 1;
		}
		for (int i = 0; i < slices; i++) {
			int botLeft = capIndex + i;
			int botRight = botLeft + 1;
			if (i == slices - 1) {
				botRight = capIndex;
			}
			mIndices[index++] = centerIndex;
			mIndices[index++] = botLeft;
			mIndices[index++] = botRight;
		}
	}
}

void Cylinder::render() {
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_NORMAL_ARRAY);
	glVertexPointer(NUM_COORDS, GL_FLOAT, 0, mVertices);
	glNormalPointer(GL_FLOAT, 0, mNormals);

	glDrawElements(GL_TRIANGLES, mNumIndices, GL_UNSIGNED_SHORT, mIndices);

	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_NORMAL_ARRAY);

	glPopMatrix();
}