
#include <cstdio>
#include <cstdlib>
#include <cmath>
#include <vector>

#include <iostream>

#ifdef __APPLE__
#  include <GL/glew.h>
#  include <GL/freeglut.h>
#  include <OpenGL/glext.h>
#else
#  include <GL/glew.h>
#  include <GL/freeglut.h>
//#  include <GL/glext.h>
#pragma comment(lib, "glew32.lib")
#endif


#define PI 3.1415926536  
#define MY_INFINITY 20000.0
#define EPSILON 0.000001
#define MAX(x,y) ((x)>(y)?(x):(y))

const int screenwidth = 800;
const int screenheight = 600;

unsigned char colorbuffer[screenwidth * screenheight * 3];

double znear = 0.01;
double ambientlight[3];
double lightpos[3];
double backcolor[3];
double lightcolor[3];

GLuint texid;

bool done = false;

inline void __copy_double3(double dest[], double src[]) {
    dest[0] = src[0]; dest[1] = src[1]; dest[2] = src[2];
}

inline void __copy_double32(double dest[], double x, double y, double z) {
    dest[0] = x; dest[1] = y; dest[2] = z;
}
inline void CROSS(double dest[3], double v1[3], double v2[3]) {
    __copy_double32(dest, v1[1] * v2[2] - v1[2] * v2[1], v1[2] * v2[0] - v1[0] * v2[2], v1[0] * v2[1] - v1[1] * v2[0]);
}
inline double DOT(double v1[3], double v2[3]) {
    return v1[0] * v2[0] + v1[1] * v2[1] + v1[2] * v2[2];
}
inline void ADD(double dest[3], double v1[3], double v2[3]) {
    __copy_double32(dest, v1[0] + v2[0], v1[1] + v2[1], v1[2] + v2[2]);
}
inline void SUB(double dest[3], double v1[3], double v2[3]) {
    __copy_double32(dest, v1[0] - v2[0], v1[1] - v2[1], v1[2] - v2[2]);
}
inline void __normalize_double3(double n[]) {
    double l = sqrt(DOT(n, n));
    if (l > EPSILON) {
        n[0] /= l; n[1] /= l; n[2] /= l;
    }
    else {
        n[0] = 0.0; n[1] = 0.0; n[2] = 0.0;
    }
}

#define OBJECT_MESH		0x1
#define OBJECT_SPHERE 	0x2
struct _object_base {
    int type;
    double ks[3];	//specular property
    double kd[3];
    double kr[3];
    double kn;	//shining or roughness
    double nr;
    _object_base(int t) : type(t), nr(1.0) { __copy_double32(ks, 0.0, 0.0, 0.0); __copy_double32(kr, 0.0, 0.0, 0.0); }
};

static const double __CUBE_POS[8][3] = {
    {0,0,0},{1,0,0},{1,1,0},{0,1,0},{0,0,1},{1,0,1},{1,1,1},{0,1,1}
};
static const int __CUBE_FACEVERTS[12][3] = {
    {2,6,7},{2,7,3},{0,4,5},{0,5,1},{6,1,5},{6,2,1},{3,7,4},{3,4,0},{4,7,6},{4,6,5},{3,0,1},{3,1,2}
};
static const double __CUBE_FACENORMS[12][3] = {
    {0,1,0},{0,1,0},{0,-1,0},{0,-1,0},{1,0,0},{1,0,0},{-1,0,0},{-1,0,0},{0,0,1},{0,0,1},{0,0,-1},{0,0,-1}
};
struct _object_box : _object_base {
    double pos[3], size[3], color[3];
    _object_box() : _object_base(OBJECT_MESH) {}
    bool HitBoundbox(double eye[3], double dir[3]) {
        return true;
    }
};

struct _object_sphere : _object_base {
    double center[3];
    double radius;
    _object_sphere() : _object_base(OBJECT_SPHERE) {}
};

std::vector<_object_base*> objects;

void initObjects()
{
    //Create floating spheres and thick glass
    _object_sphere* s1 = new _object_sphere();
    __copy_double32(s1->center, 3.0, 2.0, 1.0);
    (s1->radius) = 0.5;
    __copy_double32(s1->ks, 0.6, 0.6, 0.6);
    __copy_double32(s1->kr, 0.0, 0.0, 0.0);
    __copy_double32(s1->kd, 1.0, 1.0, 1.0);
    s1->kn = 100;
    s1->nr = 1;
    objects.push_back(s1);

    _object_sphere* s2 = new _object_sphere();
    __copy_double32(s2->center, 2.0, 3.0, 2.0);
    (s2->radius) = 0.5;
    __copy_double32(s2->ks, 1.0, 1.0, 1.0);
    __copy_double32(s2->kr, 0.0, 0.0, 0.0);
    __copy_double32(s2->kd, 0.0, 0.5, 1.0);
    s2->kn = 100;
    s2->nr = 1;
    objects.push_back(s2);

    _object_sphere* s4 = new _object_sphere();
    __copy_double32(s4->center, 1.75, 4.25, 2.5);
    (s4->radius) = 0.25;
    __copy_double32(s4->ks, 0.0, 0.0, 0.0);
    __copy_double32(s4->kr, 0.8, 0.8, 0.8);
    __copy_double32(s4->kd, 0.2, 0.3, 0.4);
    s4->kn = 0;
    s4->nr = 2.5;
    objects.push_back(s4);

    _object_box* m0 = new _object_box();
    __copy_double32(m0->pos, 3.5, 3.2, 1.5);
    __copy_double32(m0->size, 0.3, 1.5, 1.5);
    __copy_double32(m0->color, 0.2, 0.3, 0.4);
    __copy_double32(m0->ks, 0, 0, 0);
    __copy_double32(m0->kr, 0.8, 0.8, 0.8);
    __copy_double32(m0->kd, 0.5, 0.5, 1.0);
    (m0->kn) = 0;
    (m0->nr) = 2.5;
    objects.push_back(m0);

    _object_box* m0a = new _object_box();
    __copy_double32(m0a->pos, 2, 4.25, 2.5);
    __copy_double32(m0a->size, 0.1, 1, 0.5);
    __copy_double32(m0a->color, 0.2, 0.3, 0.4);
    __copy_double32(m0a->ks, 0, 0, 0);
    __copy_double32(m0a->kr, 0.8, 0.8, 0.8);
    __copy_double32(m0a->kd, 0.5, 0.5, 1.0);
    (m0a->kn) = 0;
    (m0a->nr) = 2.5;
    objects.push_back(m0a);


    //create walls
    _object_box* m1 = new _object_box();
    __copy_double32(m1->pos, 0, 0, -0.1);
    __copy_double32(m1->size, 5.5, 5.5, 0.1);
    __copy_double32(m1->color, 0.1, 1, 0.5);
    __copy_double32(m1->ks, 0, 0, 0);
    __copy_double32(m1->kr, 0, 0, 0);
    __copy_double32(m1->kd, 0.5, 0.5, 1.0);
    (m1->kn) = 0;
    (m1->nr) = 1.0;
    objects.push_back(m1);

    _object_box* m2 = new _object_box();
    __copy_double32(m2->pos, 0, -0.1, 0);
    __copy_double32(m2->size, 5.5, 0.1, 4);
    __copy_double32(m2->color, 1.0, 0.2, 0.8);
    __copy_double32(m2->ks, 0, 0, 0);
    __copy_double32(m2->kr, 0, 0, 0);
    __copy_double32(m2->kd, 0.5, 0.5, 1.0);
    (m2->kn) = 10;
    (m2->nr) = 1.0;
    objects.push_back(m2);

    _object_box* m3 = new _object_box();
    __copy_double32(m3->pos, -0.1, 0, 0);
    __copy_double32(m3->size, 0.1, 5.5, 4);
    __copy_double32(m3->color, 1.0, 0.2, 0.8);
    __copy_double32(m3->ks, 0, 0, 0);
    __copy_double32(m3->kr, 0, 0, 0);
    __copy_double32(m3->kd, 0.5, 0.5, 1.0);
    (m3->kn) = 0;
    (m3->nr) = 1.0;
    objects.push_back(m3);

    //create mirror
    _object_box* m4 = new _object_box();
    __copy_double32(m4->pos, 1, 0, 1.5);
    __copy_double32(m4->size, 2.2, 0.1, 1.5);
    __copy_double32(m4->color, 0.0, 0.2, 0.8);
    __copy_double32(m4->ks, 0.5, 0.5, 0.5);
    __copy_double32(m4->kr, 0, 0, 0);
    __copy_double32(m4->kd, 0.5, 0.5, 1.0);
    (m4->kn) = 10;
    (m4->nr) = 1.0;
    objects.push_back(m4);

    //create bed
    _object_box* m5 = new _object_box();
    __copy_double32(m5->pos, 0, 0, 0);
    __copy_double32(m5->size, 3.5, 2.3, 0.3);
    __copy_double32(m5->color, 0.2, 0.3, 0.4);
    __copy_double32(m5->ks, 0, 0, 0);
    __copy_double32(m5->kr, 0, 0, 0);
    __copy_double32(m5->kd, 0, 0, 0);
    (m5->kn) = 0;
    (m5->nr) = 2.5;
    objects.push_back(m5);

    _object_box* m5a = new _object_box();
    __copy_double32(m5a->pos, 0, 0, 0);
    __copy_double32(m5a->size, 0.3, 2.3, 1);
    __copy_double32(m5a->color, 0.2, 0.3, 0.4);
    __copy_double32(m5a->ks, 0, 0, 0);
    __copy_double32(m5a->kr, 0, 0, 0);
    __copy_double32(m5a->kd, 0, 0, 0);
    (m5a->kn) = 0;
    (m5a->nr) = 2.5;
    objects.push_back(m5a);

    _object_box* m6 = new _object_box();
    __copy_double32(m6->pos, 0.3, 0.1, 0.3);
    __copy_double32(m6->size, 3.1, 2.1, 0.15);
    __copy_double32(m6->color, 1, 1, 1);
    __copy_double32(m6->ks, 0, 0, 0);
    __copy_double32(m6->kr, 0, 0, 0);
    __copy_double32(m6->kd, 0, 0, 0);
    (m6->kn) = 0;
    (m6->nr) = 2.5;
    objects.push_back(m6);

    //create dresser
    _object_box* m7 = new _object_box();
    __copy_double32(m7->pos, 0, 4.25, 0);
    __copy_double32(m7->size, 0.5, 1, 1.6);
    __copy_double32(m7->color, 1, 1, 0.5);
    __copy_double32(m7->ks, 0, 0, 0);
    __copy_double32(m7->kr, 0, 0, 0);
    __copy_double32(m7->kd, 0, 0, 0);
    (m7->kn) = 0;
    (m7->nr) = 0;
    objects.push_back(m7);

    _object_box* m8 = new _object_box();
    __copy_double32(m8->pos, 0, 4.35, 0.1);
    __copy_double32(m8->size, 0.55, 0.8, 0.4);
    __copy_double32(m8->color, 1, 1, 0.5);
    __copy_double32(m8->ks, 0, 0, 0);
    __copy_double32(m8->kr, 0, 0, 0);
    __copy_double32(m8->kd, 0, 0, 0);
    (m8->kn) = 0;
    (m8->nr) = 0;
    objects.push_back(m8);

    _object_box* m9 = new _object_box();
    __copy_double32(m9->pos, 0, 4.35, 0.6);
    __copy_double32(m9->size, 0.55, 0.8, 0.4);
    __copy_double32(m9->color, 1, 1, 0.5);
    __copy_double32(m9->ks, 0, 0, 0);
    __copy_double32(m9->kr, 0, 0, 0);
    __copy_double32(m9->kd, 0, 0, 0);
    (m9->kn) = 0;
    (m9->nr) = 0;
    objects.push_back(m9);

    _object_box* m10 = new _object_box();
    __copy_double32(m10->pos, 0, 4.35, 1.1);
    __copy_double32(m10->size, 0.55, 0.8, 0.4);
    __copy_double32(m10->color, 1, 1, 0.5);
    __copy_double32(m10->ks, 0, 0, 0);
    __copy_double32(m10->kr, 0, 0, 0);
    __copy_double32(m10->kd, 0, 0, 0);
    (m10->kn) = 0;
    (m10->nr) = 0;
    objects.push_back(m10);


    //Create night stand
    _object_box* m11 = new _object_box();
    __copy_double32(m11->pos, 0, 2.4, 0.7);
    __copy_double32(m11->size, 0.8, 0.8, 0.1);
    __copy_double32(m11->color, 1, 1, 0.5);
    __copy_double32(m11->ks, 0, 0, 0);
    __copy_double32(m11->kr, 0, 0, 0);
    __copy_double32(m11->kd, 0, 0, 0);
    (m11->kn) = 0;
    (m11->nr) = 0;
    objects.push_back(m11);

    _object_box* m12 = new _object_box();
    __copy_double32(m12->pos, 0, 2.4, 0);
    __copy_double32(m12->size, 0.1, 0.1, 0.8);
    __copy_double32(m12->color, 1, 1, 0.5);
    __copy_double32(m12->ks, 0, 0, 0);
    __copy_double32(m12->kr, 0, 0, 0);
    __copy_double32(m12->kd, 0, 0, 0);
    (m12->kn) = 0;
    (m12->nr) = 0;
    objects.push_back(m12);
    _object_box* m13 = new _object_box();
    __copy_double32(m13->pos, 0.7, 2.4, 0);
    __copy_double32(m13->size, 0.1, 0.1, 0.8);
    __copy_double32(m13->color, 1, 1, 0.5);
    __copy_double32(m13->ks, 0, 0, 0);
    __copy_double32(m13->kr, 0, 0, 0);
    __copy_double32(m13->kd, 0, 0, 0);
    (m13->kn) = 0;
    (m13->nr) = 0;
    objects.push_back(m13);

    _object_box* m14 = new _object_box();
    __copy_double32(m14->pos, 0.7, 3.1, 0);
    __copy_double32(m14->size, 0.1, 0.1, 0.8);
    __copy_double32(m14->color, 1, 1, 0.5);
    __copy_double32(m14->ks, 0, 0, 0);
    __copy_double32(m14->kr, 0, 0, 0);
    __copy_double32(m14->kd, 0, 0, 0);
    (m14->kn) = 0;
    (m14->nr) = 0;
    objects.push_back(m14);

    _object_box* m15 = new _object_box();
    __copy_double32(m15->pos, 0, 3.1, 0);
    __copy_double32(m15->size, 0.1, 0.1, 0.8);
    __copy_double32(m15->color, 1, 1, 0.5);
    __copy_double32(m15->ks, 0, 0, 0);
    __copy_double32(m15->kr, 0, 0, 0);
    __copy_double32(m15->kd, 0, 0, 0);
    (m15->kn) = 0;
    (m15->nr) = 0;
    objects.push_back(m15);

    //create lamp
    _object_box* m16 = new _object_box();
    __copy_double32(m16->pos, 0.2, 2.6, 0.8);
    __copy_double32(m16->size, 0.4, 0.4, 0.07);
    __copy_double32(m16->color, 0, 1, 1);
    __copy_double32(m16->ks, 0, 0, 0);
    __copy_double32(m16->kr, 0, 0, 0);
    __copy_double32(m16->kd, 0, 0, 0);
    (m16->kn) = 0;
    (m16->nr) = 0;
    objects.push_back(m16);

    _object_box* m17 = new _object_box();
    __copy_double32(m17->pos, 0.4, 2.8, 0.8);
    __copy_double32(m17->size, 0.07, 0.07, 0.4);
    __copy_double32(m17->color, 0, 1, 1);
    __copy_double32(m17->ks, 0, 0, 0);
    __copy_double32(m17->kr, 0, 0, 0);
    __copy_double32(m17->kd, 0, 0, 0);
    (m17->kn) = 0;
    (m17->nr) = 0;
    objects.push_back(m17);

    _object_box* m18 = new _object_box();
    __copy_double32(m18->pos, 0.2, 2.6, 1.2);
    __copy_double32(m18->size, 0.4, 0.4, 0.4);
    __copy_double32(m18->color, 0, 1, 1);
    __copy_double32(m18->ks, 0, 0, 0);
    __copy_double32(m18->kr, 0, 0, 0);
    __copy_double32(m18->kd, 0, 0, 0);
    (m18->kn) = 0;
    (m18->nr) = 0;
    objects.push_back(m18);

    //create fan
    _object_box* m19 = new _object_box();
    __copy_double32(m19->pos, 4.25, 0.75, 0);
    __copy_double32(m19->size, 0.75, 0.75, 0.1);
    __copy_double32(m19->color, 0, 0, 0.5);
    __copy_double32(m19->ks, 0, 0, 0);
    __copy_double32(m19->kr, 0, 0, 0);
    __copy_double32(m19->kd, 0, 0, 0);
    (m19->kn) = 0;
    (m19->nr) = 0;
    objects.push_back(m19);

    _object_box* m20 = new _object_box();
    __copy_double32(m20->pos, 4.625, 1.125, 0.1);
    __copy_double32(m20->size, 0.1, 0.1, 1.5);
    __copy_double32(m20->color, 0, 0, 0.5);
    __copy_double32(m20->ks, 0, 0, 0);
    __copy_double32(m20->kr, 0, 0, 0);
    __copy_double32(m20->kd, 0, 0, 0);
    (m20->kn) = 0;
    (m20->nr) = 0;
    objects.push_back(m20);

    _object_sphere* s5 = new _object_sphere();
    __copy_double32(s5->center, 4.675, 1.175, 1.6);
    (s5->radius) = 0.2;
    __copy_double32(s5->ks, 0.0, 0.0, 0.0);
    __copy_double32(s5->kr, 0.0, 0.0, 0.0);
    __copy_double32(s5->kd, 0.0, 0.0, 0.5);
    s5->kn = 0;
    s5->nr = 1.1;
    objects.push_back(s5);


    _object_box* m21 = new _object_box();
    __copy_double32(m21->pos, 4.575, 1.325, 0.85);
    __copy_double32(m21->size, 0.2, 0.025, 1.5);
    __copy_double32(m21->color, 0, 0, 0.5);
    __copy_double32(m21->ks, 0, 0, 0);
    __copy_double32(m21->kr, 0, 0, 0);
    __copy_double32(m21->kd, 0, 0, 0);
    (m21->kn) = 0;
    (m21->nr) = 0;
    objects.push_back(m21);


    _object_box* m23 = new _object_box();
    __copy_double32(m23->pos, 3.875, 1.325, 1.5);
    __copy_double32(m23->size, 1.5, 0.025, 0.2);
    __copy_double32(m23->color, 0, 0, 0.5);
    __copy_double32(m23->ks, 0, 0, 0);
    __copy_double32(m23->kr, 0, 0, 0);
    __copy_double32(m23->kd, 0, 0, 0);
    (m23->kn) = 0;
    (m23->nr) = 0;
    objects.push_back(m23);

}


struct _camera_data {
    double fov;
    double eye[3];
    double lookat[3];
    double up[3];
    void getAxis(double u[3], double v[3], double w[3]) {
        SUB(w, eye, lookat);
        __normalize_double3(w);
        CROSS(u, up, w);
        __normalize_double3(u);
        CROSS(v, w, u);
        __normalize_double3(v);
    }
};

_camera_data camera;

void myInit()
{
    glClearColor(0.0, 0.0, 0.0, 1.0);
    glViewport(0, 0, screenwidth, screenheight);

    __copy_double32(camera.eye, 6.0, 6.0, 4.0);
    __copy_double32(camera.lookat, 0, 0, 0);
    __copy_double32(camera.up, -1.0, -1.0, 3);
    __normalize_double3(camera.up);
    camera.fov = 60.0;

    __copy_double32(ambientlight, 0.01, 0.01, 0.01);
    __copy_double32(lightpos, 3.0, 3.0, 8.0);
    __copy_double32(backcolor, 0.0, 0.0, 0.0);
    __copy_double32(lightcolor, 1.0, 1.0, 1.0);

    glGenTextures(1, &texid);

    initObjects();
}

void myDisplay()
{
    //Set up billboard	
    glClear(GL_COLOR_BUFFER_BIT);
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_TEXTURE_2D);

    glBindTexture(GL_TEXTURE_2D, texid);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
    glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, screenwidth, screenheight, 0, GL_RGB, GL_UNSIGNED_BYTE, colorbuffer);

    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0.0, screenwidth - 1, 0.0, screenheight - 1);

    glBegin(GL_QUADS);

    glTexCoord2f(0.0, 0.0);
    glVertex2f(0.0, 0.0);

    glTexCoord2f(1.0, 0.0);
    glVertex2f(screenwidth - 1, 0.0);

    glTexCoord2f(1.0, 1.0);
    glVertex2f(screenwidth - 1, screenheight - 1);

    glTexCoord2f(0.0, 1.0);
    glVertex2f(0.0, screenheight - 1);

    glEnd();

    glDisable(GL_TEXTURE_2D);

    glutSwapBuffers();

}

struct __ray_tracing_data {
    double kd[3], ks[3], kr[3];
    double normal[3];
    double dir[3];
    double q;
    double t;
    double nr;
};

bool ray_hit(double eye[3], double dir[3], double t0, double t1, __ray_tracing_data* rec)
{
    bool ok = false;
    for (int i = 0; i < objects.size(); i++)
    {
        // ray/triangle intersection
        if (objects[i]->type == OBJECT_MESH)
        {
            _object_box* o = (_object_box*)(objects[i]);
            //ray intersects bounding box
            if (o->HitBoundbox(eye, dir))
            {
                //for each face in current object
                for (int j = 0; j < 12; j++)
                {
                    double vert0[3], vert1[3], vert2[3];
                    __copy_double32(vert0, o->pos[0] + o->size[0] * __CUBE_POS[__CUBE_FACEVERTS[j][0]][0],
                        o->pos[1] + o->size[1] * __CUBE_POS[__CUBE_FACEVERTS[j][0]][1],
                        o->pos[2] + o->size[2] * __CUBE_POS[__CUBE_FACEVERTS[j][0]][2]);
                    __copy_double32(vert1, o->pos[0] + o->size[0] * __CUBE_POS[__CUBE_FACEVERTS[j][1]][0],
                        o->pos[1] + o->size[1] * __CUBE_POS[__CUBE_FACEVERTS[j][1]][1],
                        o->pos[2] + o->size[2] * __CUBE_POS[__CUBE_FACEVERTS[j][1]][2]);
                    __copy_double32(vert2, o->pos[0] + o->size[0] * __CUBE_POS[__CUBE_FACEVERTS[j][2]][0],
                        o->pos[1] + o->size[1] * __CUBE_POS[__CUBE_FACEVERTS[j][2]][1],
                        o->pos[2] + o->size[2] * __CUBE_POS[__CUBE_FACEVERTS[j][2]][2]);

                    double edge1[3], edge2[3], tvec[3], pvec[3], qvec[3];
                    double det, inv_det;

                    SUB(edge1, vert1, vert0);
                    SUB(edge2, vert2, vert0);
                    CROSS(pvec, dir, edge2);
                    det = DOT(edge1, pvec);

                    if (det > -EPSILON && det < EPSILON) {}
                    inv_det = 1.0 / det;

                    SUB(tvec, eye, vert0);
                    double gamma = DOT(tvec, pvec) * inv_det;
                    if (gamma < 0.0 || gamma > 1.0)
                        continue;

                    CROSS(qvec, tvec, edge1);
                    double beta = DOT(dir, qvec) * inv_det;
                    if (beta < 0.0 || beta + gamma > 1.0)
                        continue;

                    double t = DOT(edge2, qvec) * inv_det;
                    if (t0 <= t && t <= t1 && t < rec->t)
                    {
                        ok = true;
                        rec->t = t;
                        __copy_double3(rec->normal, (double*)&__CUBE_FACENORMS[j][0]);
                        __copy_double3(rec->ks, objects[i]->ks);
                        __copy_double3(rec->kd, o->color);
                        __copy_double3(rec->kr, o->kr);
                        rec->q = objects[i]->kn;
                        rec->nr = objects[i]->nr;
                        __copy_double3(rec->dir, dir);
                        __normalize_double3(rec->dir);
                    }
                }
            }
        }

        //ray sphere intersection
        if (objects[i]->type == OBJECT_SPHERE)
        {
            _object_sphere* o = (_object_sphere*)(objects[i]);
            double A = DOT(dir, dir);
            double b1[3];
            SUB(b1, eye, o->center);
            double B = DOT(dir, b1);
            double C = DOT(b1, b1) - o->radius * o->radius;

            double delta = (B * B - A * C);
            if (delta > 0.0)
            {
                double troot1 = (-B + sqrt(delta)) / A;
                double troot2 = (-B - sqrt(delta)) / A;

                if (t0 <= troot1 && troot1 <= t1 && troot1 < rec->t)
                {
                    ok = true;
                    rec->t = troot1;
                    double p[3];
                    __copy_double32(p, eye[0] + troot1 * dir[0], eye[1] + troot1 * dir[1], eye[2] + troot1 * dir[2]);
                    SUB(rec->normal, p, o->center);
                    __normalize_double3(rec->normal);

                    __copy_double3(rec->ks, objects[i]->ks);
                    __copy_double3(rec->kd, objects[i]->kd);
                    __copy_double3(rec->kr, o->kr);
                    rec->q = objects[i]->kn;
                    rec->nr = o->nr;
                    __copy_double3(rec->dir, dir);
                    __normalize_double3(rec->dir);
                }

                if (t0 <= troot2 && troot2 <= t1 && troot2 < rec->t)
                {
                    ok = true;
                    rec->t = troot2;
                    double p[3];
                    __copy_double32(p, eye[0] + troot2 * dir[0], eye[1] + troot2 * dir[1], eye[2] + troot2 * dir[2]);
                    SUB(rec->normal, p, o->center);
                    __normalize_double3(rec->normal);

                    __copy_double3(rec->ks, objects[i]->ks);
                    __copy_double3(rec->kd, objects[i]->kd);
                    __copy_double3(rec->kr, o->kr);
                    rec->q = objects[i]->kn;
                    rec->nr = o->nr;
                    __copy_double3(rec->dir, dir);
                    __normalize_double3(rec->dir);
                }
            }
        }
    }

    return ok;
}

void ray_color(double eye[3], double dir[3], double t0, double t1, int dep, double out[3])
{
    if (dep == 6)
    {
        __copy_double3(out, backcolor);
        return;
    }

    __ray_tracing_data rec, srec;
    rec.t = MY_INFINITY;
    double I[3]; //color
    if (ray_hit(eye, dir, t0, t1, &rec))
    {
        double p[3], tL[3], td[3], h[3];
        __copy_double32(p, eye[0] + rec.t * dir[0], eye[1] + rec.t * dir[1], eye[2] + rec.t * dir[2]);
        __copy_double32(I, ambientlight[0] * rec.kd[0], ambientlight[1] * rec.kd[1], ambientlight[2] * rec.kd[2]);
        double L[3];
        SUB(L, lightpos, p);
        __normalize_double3(L);
        srec.t = MY_INFINITY;

        //not in shadow
        if (!ray_hit(p, L, EPSILON, MY_INFINITY, &srec))
        {
            //l and dir should be normalized
            __copy_double3(tL, L);
            __normalize_double3(tL);
            __copy_double32(td, -dir[0], -dir[1], -dir[2]);
            __normalize_double3(td);
            ADD(h, tL, td);
            __normalize_double3(h);
            double cosalpha_n = pow((double)(DOT(h, rec.normal)), (double)rec.q);
            I[0] += (lightcolor[0] * MAX(0, DOT(rec.normal, tL)) * rec.kd[0] + lightcolor[0] * rec.ks[0] * cosalpha_n);
            I[1] += (lightcolor[1] * MAX(0, DOT(rec.normal, tL)) * rec.kd[1] + lightcolor[1] * rec.ks[2] * cosalpha_n);
            I[2] += (lightcolor[2] * MAX(0, DOT(rec.normal, tL)) * rec.kd[2] + lightcolor[2] * rec.ks[2] * cosalpha_n);
        }

        //calculate specular light if it has specular material
        if (fabs(rec.ks[0]) > 1e-8 || fabs(rec.ks[1]) > 1e-8 || fabs(rec.ks[2]) > 1e-8)
        {
            double r[3], k = DOT(rec.dir, rec.normal);
            if (k < 0.0) {
                __copy_double32(r, rec.dir[0] - 2 * k * rec.normal[0], rec.dir[1] - 2 * k * rec.normal[1], rec.dir[2] - 2 * k * rec.normal[2]);
                __normalize_double3(r);
                double sp[3];
                ray_color(p, r, EPSILON, MY_INFINITY, dep + 1, sp);
                I[0] += rec.ks[0] * sp[0];
                I[1] += rec.ks[1] * sp[1];
                I[2] += rec.ks[2] * sp[2];
            }
        }

        // transparent objects
        if (fabs(rec.kr[0]) > 1e-8 || fabs(rec.kr[1]) > 1e-8 || fabs(rec.kr[2]) > 1e-8)
        {
            double r[3], k = -DOT(rec.dir, rec.normal);
            double nr = (k > 0.0) ? rec.nr : 1.0 / rec.nr;
            double k2 = sqrt(nr * nr - 1 + k * k);
            k2 = (k > 0.0) ? (k - k2) : (k + k2);

            __copy_double32(r, rec.dir[0] + k2 * rec.normal[0], rec.dir[1] + k2 * rec.normal[1], rec.dir[2] + k2 * rec.normal[2]);
            __normalize_double3(r);
            double sp[3];
            ray_color(p, r, EPSILON, MY_INFINITY, dep + 1, sp);
            I[0] += rec.kr[0] * sp[0];
            I[1] += rec.kr[1] * sp[1];
            I[2] += rec.kr[2] * sp[2];
        }

        __copy_double3(out, I);
        return;
    }

    __copy_double3(out, backcolor);
}

void ray_trace()
{
    double aspect = (double)screenwidth / (double)screenheight;

    //Frustum
    double top = tan(camera.fov * PI / 360.0) * znear;
    double bottom = -top;
    double left = aspect * bottom;
    double right = aspect * top;
    double U[3], V[3], W[3], eye[3];
    camera.getAxis(U, V, W);
    __copy_double3(eye, camera.eye);

    //foreach pixel
    for (int y = 0; y < screenheight; y++)
    {
        for (int x = 0; x < screenwidth; x++)
        {
            //Computing the viewing ray
            double us = left + (right - left) * (x + 0.5) / screenwidth;
            double vs = bottom + (top - bottom) * (y + 0.5) / screenheight;
            double ws = -znear;
            double dir[3];
            __copy_double32(dir, us * U[0] + vs * V[0] + ws * W[0], us * U[1] + vs * V[1] + ws * W[1], us * U[2] + vs * V[2] + ws * W[2]);
            __normalize_double3(dir);
            //ray = eye + t * dir

            double colorvalue[3];
            ray_color(eye, dir, 0.0, MY_INFINITY, 1, colorvalue);
            if ((colorvalue[0] * 255 + 0.5) > 255)
                colorbuffer[3 * (y * screenwidth + x)] = 255;
            else
                colorbuffer[3 * (y * screenwidth + x)] = (unsigned char)(colorvalue[0] * 255 + 0.5);
            if ((colorvalue[1] * 255 + 0.5) > 255)
                colorbuffer[3 * (y * screenwidth + x) + 1] = 255;
            else
                colorbuffer[3 * (y * screenwidth + x) + 1] = (unsigned char)(colorvalue[1] * 255 + 0.5);
            if ((colorvalue[2] * 255 + 0.5) > 255)
                colorbuffer[3 * (y * screenwidth + x) + 2] = 255;
            else
                colorbuffer[3 * (y * screenwidth + x) + 2] = (unsigned char)(colorvalue[2] * 255 + 0.5);
        }
    }

    glutPostRedisplay();
    done = true;
    glutIdleFunc(NULL);
}

void myidle()
{
    if (!done)
    {
        glutIdleFunc(NULL);
        ray_trace();
    }
}


int main(int argc, char* argv[])
{
    glutInit(&argc, argv);                            // initialize the toolkit
    glutInitWindowSize(screenwidth, screenheight);
    glutInitDisplayMode(GLUT_DOUBLE | GLUT_RGB);     // set display mode
    glutCreateWindow(argv[0]);              // open the screen window
    myInit();
    glutDisplayFunc(myDisplay);              // register redraw function	
    glutIdleFunc(myidle);
    glutMainLoop();                          // go into a perpetual loop
    return 0;
}
