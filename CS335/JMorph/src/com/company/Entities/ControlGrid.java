package com.company.Entities;

import java.awt.*;
import java.awt.event.MouseEvent;

public class ControlGrid implements BaseDrawnEntity {
    private ControlPoint[][] pointGrid;
    private ControlPoint draggedPoint;

    private int panelWidth;
    private int panelHeight;

    /**
     * Normal constructor
     */
    public ControlGrid(int gridWidth, int gridHeight, int panelWidth, int panelHeight) {
        pointGrid = new ControlPoint[gridWidth][gridHeight];

        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;

        var widthInterval = panelWidth / gridWidth;
        var heightInterval = panelHeight / gridHeight;

        for (int i = 0; i < pointGrid.length; i++) {
            for (int j = 0; j < pointGrid[i].length; j++) {
                pointGrid[i][j] = new ControlPoint(widthInterval / 2 + widthInterval * i, heightInterval / 2 + heightInterval * j);
            }
        }
    }

    /**
     * Copy constructor
     */
    public ControlGrid(ControlGrid controlGrid) {
        pointGrid = new ControlPoint[controlGrid.getGridOfPoints().length][controlGrid.getGridOfPoints()[0].length];
        for (int i = 0; i < pointGrid.length; i++) {
            for (int j = 0; j < pointGrid[i].length; j++) {
                pointGrid[i][j] = new ControlPoint(controlGrid.getGridOfPoints()[i][j]);

            }
        }

        draggedPoint = null;
    }

    public ControlPoint getPoint(int pointX, int pointY) {
        return pointGrid[pointX][pointY];
    }

    public void beginDragging(MouseEvent e) {
        System.out.println("Clicked at: " + e.getPoint());
        for (ControlPoint[] controlPoints : pointGrid) {
            for (ControlPoint controlPoint : controlPoints) {
                var inBoundingBox = controlPoint.getBoundingBox().contains(e.getX(), e.getY());
                if (inBoundingBox) {
                    draggedPoint = controlPoint;
                    draggedPoint.beginDragging(e);
                    System.out.println("Dragging point: " + draggedPoint);
                }
            }
        }
    }

    public void doDragging(MouseEvent e, int width, int height) {
        if (draggedPoint != null) {
            draggedPoint.doDragging(e, width, height);
        }
    }

    public void endDragging(MouseEvent e) {
        if (draggedPoint != null) {
            System.out.println("Stopped dragging: " + draggedPoint);
            draggedPoint.endDragging();
            draggedPoint = null;
        }
    }

    public ControlPoint[][] getGridOfPoints() {
        return pointGrid;
    }

    @Override
    public void paintEntity(Graphics g) {
        for (ControlPoint[] controlPoints : pointGrid) {
            for (ControlPoint controlPoint : controlPoints) {
                controlPoint.paintEntity(g);
            }
        }
    }

    @Override
    public void reset() {
        var gridWidth = pointGrid.length;
        var gridHeight = pointGrid[0].length;
        var widthInterval = panelWidth / gridWidth;
        var heightInterval = panelHeight / gridHeight;

        for (int i = 0; i < pointGrid.length; i++) {
            for (int j = 0; j < pointGrid[i].length; j++) {
                pointGrid[i][j] = new ControlPoint(widthInterval / 2 + widthInterval * i, heightInterval / 2 + heightInterval * j);
            }
        }
    }
}
