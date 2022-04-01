package com.company.Entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnimatedGrid implements BaseDrawnEntity {
    List<ControlGrid> gridKeyframes = new ArrayList<>();
    ControlGrid activeFrame;

    private int currentFrame;
    private final int totalFrames;

    AnimatedGrid(List<ControlGrid> keyframes, int totalFrames) {
        activeFrame = new ControlGrid(keyframes.get(0));

        gridKeyframes.addAll(keyframes);
        this.totalFrames = totalFrames;
    }

    /**
     * Interpolate between the two grid keyframe. Return approximate interpolation at keyframe.
     */
    public Point getMorphInterpolation(int pointX, int pointY, int frameNumber) {
        var pointA = gridKeyframes.get(0).getPoint(pointX, pointY).getPosition();
        var pointB = gridKeyframes.get(1).getPoint(pointX, pointY).getPosition();

        var direction = subtract(pointA, pointB);
        // Scale current frame to be between 0 and 1.
        var frameIteration = totalFrames / frameNumber;

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

    @Override
    public void paintEntity(Graphics g) {
        activeFrame.paintEntity(g);
    }

    @Override
    public void reset() {

    }
}
