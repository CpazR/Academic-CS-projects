#include <cmath>
#include <vector>
#include <glm/glm.hpp>

class Sphere {
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
		Sphere();
		Sphere(int prec);
		void render();
		int getNumVertices();
		int getNumIndices();
		std::vector<int> getIndices();
		std::vector<glm::vec3> getVertices();
		std::vector<glm::vec2> getTexCoords();
		std::vector<glm::vec3> getNormals();
		std::vector<glm::vec3> getTangents();
};