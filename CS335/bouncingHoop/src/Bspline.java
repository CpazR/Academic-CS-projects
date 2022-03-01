import java.awt.*;

public class Bspline {

    private int xpoints[], ypoints[];
    private int limit = 4;
    private int count;
    private int width = 10;
    private int height = 10;
    private int intervals = 20;

    // constructor
    public Bspline() {
        xpoints = new int[limit];
        ypoints = new int[limit];
        count = 0;
    }

    public void paintCurve(Graphics g, Color c) {

        int xp[];
        int yp[];

        xp = new int[limit];
        yp = new int[limit];

        // must have at least 4 points
        if (count < limit)
            return;


        for (int segment = 0; segment < limit; segment++) {
            for (int count = 0, i = segment; count < limit; count++, i = (i + 1) % limit) {
                xp[count] = xpoints[i];
                yp[count] = ypoints[i];
            }
            paintCurveSegment(g, c, xp, yp);
        }
    }

    public void drawPolyline(Graphics g) {
        Graphics2D picture = (Graphics2D) g;
        picture.setStroke(new BasicStroke(3));
        picture.setColor(Color.RED);
        for (int i = 0; i < (count - 1); i++) {
            picture.fillOval(xpoints[i] - (width / 2), ypoints[i] - (height / 2),
                    width, height);
            picture.drawLine(xpoints[i], ypoints[i], xpoints[i + 1], ypoints[i + 1]);
        }
        picture.fillOval(xpoints[count - 1] - (width / 2),
                ypoints[count - 1] - (height / 2), width, height);
    }

    public void addPoint(int x, int y) {
        if (count >= limit)
            return;

        xpoints[count] = x;
        ypoints[count] = y;
        count++;
    }

    public void resetCurve() {
        count = 0;
    }

    public void setInterval(int i) {
        intervals = i;
    }

    public void paintCurveSegment(Graphics g, Color c, int[] xp, int[] yp) {
        double x, y, xold, yold, A, B, C;
        double t1, t2, t3;
        double deltax1, deltax2, deltax3;
        double deltay1, deltay2, deltay3;

        // must have at least 4 points
        if (count < limit)
            return;

        //  This is forward differencing code to draw fast Bspline
        t1 = 1.0 / intervals;
        t2 = t1 * t1;
        t3 = t2 * t1;

        //  For B-spline curve, "D" is the starting x,y coord
        //  So the first x,y coord is the D term from the cubic equation
        x = (xp[0] + 4.0 * xp[1] + xp[2]) / 6.0;
        y = (yp[0] + 4.0 * yp[1] + yp[2]) / 6.0;
        xold = x;
        yold = y;

        // set up deltas for the x-coords of B-spline
        A = (-xp[0] + 3 * xp[1] - 3 * xp[2] + xp[3]) / 6.0;
        B = (3 * xp[0] - 6 * xp[1] + 3 * xp[2]) / 6.0;
        C = (-3 * xp[0] + 3 * xp[2]) / 6.0;

        deltax1 = A * t3 + B * t2 + C * t1;
        deltax2 = 6 * A * t3 + 2 * B * t2;
        deltax3 = 6 * A * t3;

        // set up deltas for the y-coords
        A = (-yp[0] + 3 * yp[1] - 3 * yp[2] + yp[3]) / 6.0;
        B = (3 * yp[0] - 6 * yp[1] + 3 * yp[2]) / 6.0;
        C = (-3 * yp[0] + 3 * yp[2]) / 6.0;

        deltay1 = A * t3 + B * t2 + C * t1;
        deltay2 = 6 * A * t3 + 2 * B * t2;
        deltay3 = 6 * A * t3;

        Graphics2D picture = (Graphics2D) g;
        picture.setStroke(new BasicStroke(3));
        picture.setColor(c);

        for (int i = 0; i < intervals; i++) {
            x += deltax1;
            deltax1 += deltax2;
            deltax2 += deltax3;

            y += deltay1;
            deltay1 += deltay2;
            deltay2 += deltay3;

            picture.drawLine((int) xold, (int) yold, (int) x, (int) y);
            xold = x;
            yold = y;
        }

        picture.setStroke(new BasicStroke(1));
        picture.setColor(Color.BLACK);

    }
}
