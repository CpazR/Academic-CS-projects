/////////////////////////////////////////////////////////////////////
//
// Simple OpenGL program to draw a region of dimension 30x30
//
/////////////////////////////////////////////////////////////////////
#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <iostream>
using namespace std;

#define numVAOs 1

GLuint renderingProgram;
GLuint vao[numVAOs], vbo;

float rectangleWidth = 40.0f;
float rectangleHeight = 40.0f;
GLfloat rectangleVertexSize = 20.0f;
float testPos = 0.1f;

// Organized as (x, y)
float vertices[] = {
	-testPos, -testPos,
	+testPos, -testPos,

	+testPos, -testPos,
	+testPos, +testPos,

	+testPos, +testPos,
	-testPos, +testPos,

	-testPos, +testPos,
	-testPos, -testPos,
};

// Taken from program 2.3
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

GLuint createShaderProgram() {
	// Shader allows for 2d vectors to be passed in
	const char* vshaderSource =
		"#version 430    \n"
		"layout (location=0) in vec2 pos;\n"
		"void main(void) \n"
		"{ gl_Position = vec4(pos.x, pos.y, 0.0, 1.0); }";

	const char* fshaderSource =
		"#version 430    \n"
		"out vec4 color; \n"
		"void main(void) \n"
		"{ color = vec4(0.0, 0.0, 1.0, 1.0); }";

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
	glUseProgram(renderingProgram);
	glBindVertexArray(vao[0]);
	glLineWidth(rectangleVertexSize);
	glDrawArrays(GL_LINES, 0, 16);
}

int main(void) {
	if (!glfwInit()) { exit(EXIT_FAILURE); }
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	GLFWwindow* window = glfwCreateWindow(400, 400, "HW2 - Question 3; Nicholas Reel", NULL, NULL);
	glfwMakeContextCurrent(window);
	if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
	glfwSwapInterval(1);

	init(window);

	while (!glfwWindowShouldClose(window)) {
		display(window, glfwGetTime());
		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	glfwDestroyWindow(window);
	glfwTerminate();
	exit(EXIT_SUCCESS);
}
