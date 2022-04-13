package com.company.Entities;

import java.awt.*;
import java.awt.geom.Point2D;


public class Triangle {

    private Point2D.Double tri[];

    public Triangle(int x1, int y1, int x2, int y2, int x3, int y3) {
        tri = new Point2D.Double[3];
        tri[0] = new Point2D.Double((double) x1, (double) y1);
        tri[1] = new Point2D.Double((double) x2, (double) y2);
        tri[2] = new Point2D.Double((double) x3, (double) y3);
    }

    public Triangle(Polygon polygon) {
        tri = new Point2D.Double[3];
        tri[0] = new Point2D.Double(polygon.xpoints[0], polygon.ypoints[0]);
        tri[1] = new Point2D.Double(polygon.xpoints[1], polygon.ypoints[1]);
        tri[2] = new Point2D.Double(polygon.xpoints[2], polygon.ypoints[2]);
    }

    public Triangle(double[] coords) {
        if (coords.length < 6) {
            return;
        }

        tri = new Point2D.Double[3];
        int j = 0;
        for (int i = 0; i < 3; i++)
            tri[i] = new Point2D.Double(coords[j++], coords[j++]);
    }

    public double getX(int index) {
        if ((index >= 0) && (index < 6))
            return (tri[index].getX());
        System.out.println("Index out of bounds in getX()");
        return (0.0);
    }

    public double getY(int index) {
        if ((index >= 0) && (index < 6))
            return (tri[index].getY());
        System.out.println("Index out of bounds in getY()");
        return (0.0);
    }

    public void setX(int index, double x) {
        if ((index >= 0) && (index < 6)) {
            double tmpy = tri[index].getY();
            tri[index] = new Point2D.Double(x, tmpy);
        }
    }

    public void setY(int index, double y) {
        if ((index >= 0) && (index < 6)) {
            double tmpx = tri[index].getX();
            tri[index] = new Point2D.Double(tmpx, y);
        }
    }

    public double[] getAllCoords() {
        double[] allcoords = new double[6];
        allcoords[0] = tri[0].getX();
        allcoords[1] = tri[0].getY();
        allcoords[2] = tri[1].getX();
        allcoords[3] = tri[1].getY();
        allcoords[4] = tri[2].getX();
        allcoords[5] = tri[2].getY();
        return allcoords;
    }

    public double[] getXCoords() {
        double[] Xcoords = new double[3];
        Xcoords[0] = tri[0].getX();
        Xcoords[1] = tri[1].getX();
        Xcoords[2] = tri[2].getX();
        return Xcoords;
    }

    public double[] getYCoords() {
        double[] Ycoords = new double[3];
        Ycoords[0] = tri[0].getY();
        Ycoords[1] = tri[1].getY();
        Ycoords[2] = tri[2].getY();
        return Ycoords;
    }

    public void draw(Graphics g) {
        for (int i = 1; i < 3; i++) {
            g.drawLine((int) getX(i - 1), (int) getY(i - 1), (int) getX(i), (int) getY(i));
        }
        g.drawLine((int) getX(0), (int) getY(0), (int) getX(2), (int) getY(2));
    }
}
