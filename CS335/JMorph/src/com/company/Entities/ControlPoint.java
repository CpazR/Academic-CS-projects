package com.company.Entities;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * A draggable point
 */
public class ControlPoint implements BaseDrawnEntity {

    private final float radius = 10;
    private final float diameter = radius * 2;
    private Color ballColor;
    private final Point position;

    private final Rectangle boundingBox;

    private int lastKnownMouseX;
    private int lastKnownMouseY;


    ControlPoint(int xPos, int yPos) {
        position = new Point(xPos, yPos);
        ballColor = Color.BLUE;
        boundingBox = new Rectangle((int) (position.getX() - radius), (int) (position.getY() - radius), (int) diameter, (int) diameter);
    }

    public void beginDragging(MouseEvent e) {
        lastKnownMouseX = e.getXOnScreen();
        lastKnownMouseY = e.getYOnScreen();

        position.setLocation(e.getX(), e.getY());
        ballColor = Color.RED;
    }

    public void doDragging(MouseEvent e, int width, int height) {
        var deltaX = e.getXOnScreen() - lastKnownMouseX;
        var deltaY = e.getYOnScreen() - lastKnownMouseY;

        updatedPosition(position.getX() + deltaX, position.getY() + deltaY);
        applyPositionBoundary(width, height);

        lastKnownMouseX = e.getXOnScreen();
        lastKnownMouseY = e.getYOnScreen();
    }

    private void updatedPosition(double xNew, double yNew) {
        position.setLocation(xNew, yNew);
        boundingBox.setLocation((int) xNew, (int) yNew);
    }

    private void applyPositionBoundary(int width, int height) {
        if (position.getX() < 0) {
            updatedPosition(1, position.getY());
        }
        if (position.getY() < 0) {
            updatedPosition(position.getX(), 1);
        }

        if (position.getX() > width) {
            updatedPosition(width - 1, position.getY());
        }
        if (position.getY() > height) {
            updatedPosition(position.getX(), height - 1);
        }
    }

    public void endDragging() {
        ballColor = Color.BLUE;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public Point getPosition() {
        return position;
    }

    public void updatePosition(Point newPosition) {
        updatedPosition(newPosition.getX(), newPosition.getY());
    }

    @Override
    public void paintEntity(Graphics g) {
        g.setColor(ballColor);
        g.fillOval((int) (position.getX() - radius), (int) (position.getY() - radius), (int) diameter, (int) diameter);
    }

    @Override
    public void reset() {

    }
}
