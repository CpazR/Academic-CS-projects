package Entities;

import ApplicationGUI.ApplicationContext;

import java.awt.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Contains four points and data containing curves
 */
public class Hoop implements BaseDrawnEntity {

    /**
     * Points of hoop to interpolate splines between.
     * 0 and 1 are the initial top points of hoop
     * 2 and 3 are initial bottom points of hoop
     */
    private final Ball[] hoopPoints = new Ball[4];
    private final Bspline hoopCurve = new Bspline();

    private boolean drawPoints = true;
    private boolean drawCurve = true;

    public Hoop() {
        for (int i = 0; i < hoopPoints.length; i++) {
            var hoopColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            var colorThreshold = 222;
            // If collectively all colors components are too high, redo color. Keeps a good contrast with background.
            while (hoopColor.getRed() > colorThreshold && hoopColor.getGreen() > colorThreshold && hoopColor.getBlue() > colorThreshold) {
                hoopColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
            }
            hoopPoints[i] = new Ball(ApplicationContext.panelWidth, ApplicationContext.panelHeight,
                    (float) (ApplicationContext.applicationSpeed * Math.random()),
                    (float) (ApplicationContext.applicationSpeed * Math.random()),
                    hoopColor);
        }
    }

    public void togglePoints() {
        drawPoints = !drawPoints;
    }

    public void toggleCurve() {
        drawCurve = !drawCurve;
    }

    /**
     * Draws each point together
     */
    @Override
    public void paintEntity(Graphics g) {
        for (int i = 0; i < hoopPoints.length; i++) {
            var currentPoint = hoopPoints[i];
            if (drawPoints) {
                currentPoint.paintBall(g);
            }

        }
        if (drawCurve) {
            hoopCurve.paintCurve(g, Arrays.stream(hoopPoints).map(Ball::getColor).collect(Collectors.toList()));
        }
    }

    /**
     * Entities.Hoop will move each point together
     */
    @Override
    public void move() {
        // Update point positions
        hoopCurve.resetCurve();
        for (int i = 0; i < hoopPoints.length; i++) {
            hoopPoints[i].moveBall();

            hoopCurve.addPoint(hoopPoints[i].getX(), hoopPoints[i].getY());
        }
    }
}
