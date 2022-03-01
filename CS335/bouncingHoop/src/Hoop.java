import java.awt.*;

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

    private final boolean drawPoints;

    Hoop(boolean drawPoints) {
        this.drawPoints = drawPoints;

        for (int i = 0; i < hoopPoints.length; i++) {
            hoopPoints[i] = new Ball(ApplicationContext.panelWidth, ApplicationContext.panelHeight,
                    (float) (ApplicationContext.applicationSpeed * Math.random()),
                    (float) (ApplicationContext.applicationSpeed * Math.random()),
                    new Color((float) Math.random(), (float) Math.random(), (float) Math.random()));
        }
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

            var nextPoint = (i != 3) ? hoopPoints[i + 1] : hoopPoints[0];
//            g.drawLine(currentPoint.getX(), currentPoint.getY(), nextPoint.getX(), nextPoint.getY());
            hoopCurve.paintCurve(g, currentPoint.getColor());
        }
    }

    /**
     * Hoop will move each point together
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
