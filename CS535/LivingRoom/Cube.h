#pragma once

class Cube {
	private:
		float xOrigin;
		float yOrigin;
		unsigned int VBO;
		unsigned int VAO;
		int numVertices;
		int numIndices;
		std::vector<int> indices;
		std::vector<glm::vec3> vertices;
		std::vector<glm::vec2> texCoords;
		std::vector<glm::vec3> normals;
		std::vector<glm::vec3> tangents;
		glm::mat4 transformations;
		void init(int);
		float toRadians(float degrees);
	public:
		Cube(float verticesSize);
		void render();
		void init();
};