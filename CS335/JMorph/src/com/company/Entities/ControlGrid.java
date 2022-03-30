package com.company.Entities;

import java.awt.*;

public class ControlGrid {
    private ControlPoint[][] pointGridA;
    private ControlPoint[][] pointGridB;

    ControlGrid(int gridWidth, int gridHeight) {
        pointGridA = new ControlPoint[gridWidth][gridHeight];
        pointGridB = new ControlPoint[gridWidth][gridHeight];
    }

    public Point getMorphInterpolation(int pointX, int pointY, int frameNumber) {
        var direction = subtract(pointGridA[pointX][pointY].getPosition(), pointGridB[pointX][pointY].getPosition());

        return new Point();
    }

    public static Point subtract(Point p1, Point p2) {
        return new Point((int) (p1.getX() - p2.getX()), (int) (p1.getY() - p2.getY()));
    }
}
