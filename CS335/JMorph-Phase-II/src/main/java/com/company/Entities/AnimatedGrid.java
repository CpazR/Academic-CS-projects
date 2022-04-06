package com.company.Entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimatedGrid implements BaseDrawnEntity {
    private List<ControlGrid> gridKeyframes = new ArrayList<>();
    private ControlGrid activeFrame;
    private List<Polygon> pointTriangles = new ArrayList<>();

    private int currentFrame;
    private final int totalFrames;

    public AnimatedGrid(List<ControlGrid> keyframes, int totalFrames) {
        activeFrame = new ControlGrid(keyframes.get(0));

        gridKeyframes.addAll(keyframes);
        this.totalFrames = totalFrames;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    /**
     * Interpolate between the two grid keyframe. Return approximate interpolation at keyframe.
     */
    public Point getMorphInterpolation(int pointX, int pointY, int frameNumber) {
        var pointA = gridKeyframes.get(0).getPoint(pointX, pointY).getPosition();
        var pointB = gridKeyframes.get(1).getPoint(pointX, pointY).getPosition();

        var direction = subtract(pointB, pointA);
        // Scale current frame to be between 0 and 1.
        double frameIteration = (double) frameNumber / totalFrames;

        var interpolatedX = pointA.getX() + direction.getX() * frameIteration;
        var interpolatedY = pointA.getY() + direction.getY() * frameIteration;

        return new Point((int) interpolatedX, (int) interpolatedY);
    }

    /**
     * Point arithmetic that isn't built in for some reason
     */
    public Point subtract(Point p1, Point p2) {
        return new Point((int) (p1.getX() - p2.getX()), (int) (p1.getY() - p2.getY()));
    }

    /**
     * Animate grid one point at a time
     */
    public boolean animate(int currentFrame) {
        this.currentFrame = currentFrame;
        var continues = true;
        if (this.currentFrame <= totalFrames) {

            System.out.println("Animating frame: " + this.currentFrame);
            var pointGrid = activeFrame.getGridOfPoints();

            for (int i = 0; i < pointGrid.length; i++) {
                for (int j = 0; j < pointGrid[i].length; j++) {
                    var currentPoint = pointGrid[i][j];
                    var newPosition = getMorphInterpolation(i, j, this.currentFrame);
                    currentPoint.updatePosition(newPosition);
                }
            }

            activeFrame.updateTriangles();
        } else {
            continues = false;
        }

        return continues;
    }

    @Override
    public void paintEntity(Graphics g) {
        activeFrame.paintEntity(g);
    }

    @Override
    public void reset() {

    }
}
