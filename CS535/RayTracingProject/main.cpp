/**
*
* Refactored project 2 to work with 
*
**/
using namespace std;

#include <iostream>
#include <stack>

#include <GL/glew.h>
#include <GLFW/glfw3.h>
#include <glm/gtc/type_ptr.hpp>
#include <glm/gtc/matrix_transform.hpp>

#include "Utils.h"

const int WINDOW_WIDTH = 800;
const int WINDOW_HEIGHT = 600;
float aspectRatio;

int workGroupsX = WINDOW_WIDTH;
int workGroupsY = WINDOW_HEIGHT;
int workGroupsZ = 1;


// ------------- Rendering properties ------------ //
GLuint VAO;
GLuint VBO[2];

GLuint raytraceComputeShader, screenQuadShader;
GLuint displayRenderTextureId;
unsigned char* displayRenderTexture;

// This method is the main rendering function for the scene. It calls the ray tracing compute shader.
void displayScene() {
    glUseProgram(raytraceComputeShader);

    // Bind the screen_texture_id texture to an image unit as the compute shader's output
    glBindImageTexture(0, displayRenderTextureId, 0, GL_FALSE, 0, GL_WRITE_ONLY, GL_RGBA8);

    glActiveTexture(GL_TEXTURE0);

    glDispatchCompute(workGroupsX, workGroupsY, workGroupsZ);
    glMemoryBarrier(GL_ALL_BARRIER_BITS);

    // Screen has been rendered by compute shader and inserted into texture, draw using buffer
    glUseProgram(screenQuadShader);

    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, displayRenderTextureId);

    glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
    glEnableVertexAttribArray(0);

    glBindBuffer(GL_ARRAY_BUFFER, VBO[1]);
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
    glEnableVertexAttribArray(1);

    glDrawArrays(GL_TRIANGLES, 0, 6);
}

void init() {
    Utils::displayComputeShaderLimits();

    // Allocate memory for rendered screen texture
    displayRenderTexture = (unsigned char*)malloc(sizeof(unsigned char) * 4 * WINDOW_WIDTH * WINDOW_HEIGHT);
    memset(displayRenderTexture, 0, sizeof(char) * 4 * WINDOW_WIDTH * WINDOW_HEIGHT);

    // Set default pixel values
    for (int i = 0; i < WINDOW_HEIGHT; i++) {
        for (int j = 0; j < WINDOW_WIDTH; j++) {
            // Iterate through texture and manually set its RGBA values
            int pixelNum = i * WINDOW_WIDTH * 4 * j * 4;
            displayRenderTexture[pixelNum + 0] = 250;
            displayRenderTexture[pixelNum + 1] = 128;
            displayRenderTexture[pixelNum + 2] = 255;
            displayRenderTexture[pixelNum + 3] = 255;
        }
    }

    // Create texture for OpenGL to process in buffers
    glGenTextures(1, &displayRenderTextureId);
    glBindTexture(GL_TEXTURE_2D, displayRenderTextureId);

    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, WINDOW_WIDTH, WINDOW_HEIGHT, 0, GL_RGBA, GL_UNSIGNED_BYTE, displayRenderTexture);

    // Creating Fullscreen Quad Vertices and texture coordinates
    const float windowQuadVerts[] =
    { -1.0f, 1.0f, 0.3f,  -1.0f,-1.0f, 0.3f,  1.0f, -1.0f, 0.3f,
        1.0f, -1.0f, 0.3f,  1.0f,  1.0f, 0.3f,  -1.0f,  1.0f, 0.3f
    };
    const float windowQuadUVs[] =
    { 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f,
        1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f
    };

    // Bind vertices and texture UVs to buffers
    glGenVertexArrays(1, &VAO);
    glBindVertexArray(VAO);
    glGenBuffers(2, VBO);

    // For vertices
    glBindBuffer(GL_ARRAY_BUFFER, VBO[0]);
    glBufferData(GL_ARRAY_BUFFER, sizeof(windowQuadVerts), windowQuadVerts, GL_STATIC_DRAW);
    // For texture UVs
    glBindBuffer(GL_ARRAY_BUFFER, VBO[1]); 
    glBufferData(GL_ARRAY_BUFFER, sizeof(windowQuadUVs), windowQuadUVs, GL_STATIC_DRAW);

    raytraceComputeShader = Utils::createShaderProgram("raytraceComputeShader.glsl");
    screenQuadShader = Utils::createShaderProgram("vertShader.glsl", "fragShader.glsl");
}

void windowSizeCallback(GLFWwindow* win, int newWidth, int newHeight) {
    glViewport(0, 0, newWidth, newHeight);
}

int main(int, char**) {
    // GLFW initialization
    if (!glfwInit()) { return -1; }
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
    GLFWwindow* appWindow = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Program 3 - Nicholas Reel", NULL, NULL);
    glfwMakeContextCurrent(appWindow);
    if (glewInit() != GLEW_OK) { exit(EXIT_FAILURE); }
    glfwSwapInterval(1); // vsync enabled

    glDisable(GL_CULL_FACE);


    glfwSetWindowSizeCallback(appWindow, windowSizeCallback);

    init();

    glUseProgram(raytraceComputeShader);

    while (!glfwWindowShouldClose(appWindow)) {
        displayScene();
        glfwSwapBuffers(appWindow);
        glfwPollEvents();
    }
    
    glfwDestroyWindow(appWindow);
    glfwTerminate();
    exit(EXIT_SUCCESS);
}