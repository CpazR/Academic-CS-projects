///
/// Base OpenGL file template
/// Includes all basic imports and very rudimentary setup for execution
///

#include<Windows.h>
#include<GL/GL.h>
#include<GLFW/glfw3.h>

#include<iostream>

int main() {

	if (!glfwInit()) {
		std::cerr << "Base OpenGL program built and failed to initialize. Check configuration.";
	}
	else {
		std::cout << "Base OpenGL program built and works.";
	}

	GLFWwindow* window = glfwCreateWindow(640, 480, "OpenGl GLFW Template File", NULL, NULL);

	if (!window) {
		std::cerr << "Window initialization failed! Check configuration.";
	}

	while (!glfwWindowShouldClose(window)) {
		glClearColor(1.0, 0.0, 0.0, 1.0);
		glClear(GL_DEPTH_BUFFER_BIT);

		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	glfwDestroyWindow(window);
	glfwTerminate();
	exit(EXIT_SUCCESS);

	return 0;
}