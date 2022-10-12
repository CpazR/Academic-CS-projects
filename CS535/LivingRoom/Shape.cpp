#include "Shape.h"

Shape::Shape(void) {
}


Shape::~Shape(void) {
	if (mVertices) {
		delete[] mVertices;
	}
	if (mIndices) {
		delete[] mIndices;
	}
	if (mNormals) {
		delete[] mNormals;
	}
}

void Shape::render(void) {
}