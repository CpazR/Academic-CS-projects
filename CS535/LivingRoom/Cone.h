#pragma once
#include "Shape.h"
#include "Vector3f.h"

/**
* This class defines a cone primitive and handles the generation and rendering of it.
* Megan Worley
*/
class Cone : public Shape {
	public:
		Cone(void);

		// Creates a cone along the z-axis.
		// Params:
		//		base - The radius of the base.
		//		height - The height of the cone.
		//		slices - The number of subdivisions around the z-axis.
		//		stacks - The number of subdivions along the z-axis.
		Cone(GLdouble base, GLdouble height, GLint slices, GLint stacks);
		void render();
	private:
			int mNumIndices;
};
