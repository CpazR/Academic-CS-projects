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
	// Calculate the vertices of the sides of the cylinder.
	std::vector<glm::vec3> mTempVertices = std::vector<glm::vec3>(numVertices);
	mNormals = std::vector<glm::vec3>(numVertices);
	mIndices = std::vector <GLushort>(mNumIndices);
	float epsilon = 0.2f;
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
			// Populate the vectors
			mTempVertices[index] = glm::vec3(x, y, z);
			mNormals[index++] = glm::vec3(n.x, n.y, n.z);
		}
		z += slopeStep;
		base -= radiusStep;
	}

	// Add the vertices and normals for the base.
	int segmentLength = slices;
	for (int i = 0; i < segmentLength; i++) {
		glm::vec3 vertex = mTempVertices[i];
		GLfloat x = vertex.x;
		GLfloat y = vertex.y;
		GLfloat z = vertex.z;
		mTempVertices[index] = glm::vec3(x, y, z);
		mNormals[index++] = glm::vec3(0, 0, -1);
	}
	// Center point of base.
	mTempVertices[index] = glm::vec3(0, 0, 0);
	mNormals[index++] = glm::vec3(0, 0, -1);

	// Calculate the indices for the triangles that make up the sides.
	index = 0;
	for (int rows = 0; rows < stacks; rows++) {
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
	int baseIndex = slices * (stacks + 1);
	int centerIndex = slices * (stacks + 2);
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

	for (int i = 0; i < mNumIndices; i++) {
		mVertices.push_back(mTempVertices[mIndices[i]]);
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