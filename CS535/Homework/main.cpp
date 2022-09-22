/////////////////////////////////////////////////////////////////////
//
// Simple OpenGL program to draw a region of dimension 30x30
//
/////////////////////////////////////////////////////////////////////
#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <iostream>
#include <fstream>
#include <string>

#include "glm/mat4x4.hpp"
#include "glm/gtc/type_ptr.hpp"
#include "glm/gtc/matrix_transform.hpp"
using namespace std;

#define numVAOs 1

GLuint renderingProgram;
GLuint vao[numVAOs], vbo;
GLfloat timeInterval;

float polygonPos = 60.0f;

// Organized as (x, y)
float vertices[] = {
	// Lower rectangle
	-polygonPos, -polygonPos,
	+polygonPos, -polygonPos,
	+polygonPos, -polygonPos / 2,

	-polygonPos, -polygonPos / 2,
	+polygonPos, -polygonPos / 2,
	-polygonPos, -polygonPos,

	// Top rectangle
	+polygonPos, +polygonPos,
	-polygonPos, +polygonPos,
	-polygonPos, +polygonPos / 2,

	+polygonPos, +polygonPos / 2,
	-polygonPos, +polygonPos / 2,
	+polygonPos, +polygonPos,

	// Right rectangle
	+polygonPos, +polygonPos,
	+polygonPos, -polygonPos,
	+polygonPos / 2, +polygonPos,

	+polygonPos / 2, +polygonPos,
	+polygonPos, -polygonPos,
	+polygonPos / 2, -polygonPos,

	// Left rectangle
	-polygonPos, +polygonPos,
	-polygonPos, -polygonPos,
	-polygonPos / 2, +polygonPos,

	-polygonPos / 2, +polygonPos,
	-polygonPos, -polygonPos,
	-polygonPos / 2, -polygonPos,
};

// Taken from program 2.3 for debugging purposes
void printShaderLog(GLuint shader) {
	int len = 0;
	int chWrittn = 0;
	char* log;
	glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &len);
	if (len > 0)
	{
		log = (char*)malloc(len);
		glGetShaderInfoLog(shader, len, &chWrittn, log);
		cout << "Shader Info Log: " << log << endl;
		free(log);
	}
}

// Taken from program 2.5
string readFile(const char* filePath) {
	string content;
	ifstream fileStream(filePath, ios::in);
	string line;
	while (!fileStream.eof()) {
		getline(fileStream, line);
		content.append(line + "\n");
	}
	fileStream.close();
	return content;
}

GLuint createShaderProgram() {
	// Shader allows for 2d vectors to be passed in for variable positions
	string vertShaderStr = readFile("vertShader.glsl");
	string fragShaderStr = readFile("fragShader.glsl");
	const char* vshaderSource = vertShaderStr.c_str();
	const char* fshaderSource = fragShaderStr.c_str();

	GLuint vShader = glCreateShader(GL_VERTEX_SHADER);
	GLuint fShader = glCreateShader(GL_FRAGMENT_SHADER);
	GLint vertCompiled;
	GLint fragCompiled;
	GLuint vfprogram = glCreateProgram();

	glShaderSource(vShader, 1, &vshaderSource, NULL);
	glShaderSource(fShader, 1, &fshaderSource, NULL);
	glCompileShader(vShader);
	glGetShaderiv(vShader, GL_COMPILE_STATUS, &vertCompiled);
	if (vertCompiled != 1) {
		cout << "Vertex shader compilation failed" << endl;
		printShaderLog(vShader);
	} else {
		cout << "Vertex shader compilation successful" << endl;
	}

	glCompileShader(fShader);
	glGetShaderiv(fShader, GL_COMPILE_STATUS, &fragCompiled);
	if (fragCompiled != 1) {
		cout << "Fragment shader compilation failed" << endl;
		printShaderLog(fShader);
	} else {
		cout << "Fragment shader compilation successful" << endl;
	}

	glAttachShader(vfprogram, vShader);
	glAttachShader(vfprogram, fShader);
	glLinkProgram(vfprogram);

	return vfprogram;
}

void init(GLFWwindow* window) {
	renderingProgram = createShaderProgram();
	// Establish vao and vbo buffers
	glGenVertexArrays(numVAOs, vao);
	glGenBuffers(1, &vbo);
	glBindVertexArray(vao[0]);

	glBindBuffer(GL_ARRAY_BUFFER, vbo);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vertices), vertices, GL_STATIC_DRAW);

	// Pipe buffer for shader input
	glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 2 * sizeof(float), NULL);
	glEnableVertexAttribArray(0);
}

void display(GLFWwindow* window, double currentTime) {
	// Clear screen and recalculate shader
	glClearColor(1.0, 1.0, 1.0, 1.0);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glUseProgram(renderingProgram);
	// Update time constant
	const int timeUniformLocation = glGetUniformLocation(renderingProgram, "u_time");
	glUniform1f(timeUniformLocation, timeInterval);
	timeInterval += 0.01f;
	// Draw vertices
	glBindVertexArray(vao[0]);
	glDrawArrays(GL_TRIANGLES, 0, 36);
}

int main(void) {
	double displayWidth  = 400;
	double displayHeight = 400;
	if (!glfwInit()) { exit(EXIT_FAILURE); }
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	GLFWwindow* window = glfwCreateWindow(displayWidth, displayHeight, "HW3 - Question 1; Nicholas Reel", NULL, NULL);
	glfwMakeContextCurrent(window);
	if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
	glfwSwapInterval(1);

	init(window);


	GLuint projMatLoc = glGetUniformLocation(renderingProgram, "u_projMat");
	GLuint translationVecLoc = glGetUniformLocation(renderingProgram, "u_transVec");

	while (!glfwWindowShouldClose(window)) {

		display(window, glfwGetTime());
		glm::mat4 projMatrix = glm::ortho(0.0, displayWidth, displayHeight, 0.0);
		glUniformMatrix4fv(projMatLoc, 1, GL_FALSE, value_ptr(projMatrix));

		glm::mat4 translateMatrix = glm::translate(projMatrix, glm::vec3(displayWidth / 2, displayHeight / 2, 0.0));
		glUniformMatrix4fv(translationVecLoc, 1, GL_FALSE, value_ptr(translateMatrix));

		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	glfwDestroyWindow(window);
	glfwTerminate();
	return EXIT_SUCCESS;
}
