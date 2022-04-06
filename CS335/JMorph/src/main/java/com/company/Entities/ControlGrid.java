package com.company.Entities;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class ControlGrid implements BaseDrawnEntity {
    private final ControlPoint[][] pointGrid;
    private ControlPoint draggedPoint;
    private final List<Polygon> pointTriangles = new ArrayList<>();

    private int panelWidth;
    private int panelHeight;

    /**
     * Normal constructor
     */
    public ControlGrid(int gridWidth, int gridHeight, int panelWidth, int panelHeight) {

        pointGrid = new ControlPoint[gridWidth + 2][gridHeight + 2];

        // Add two for hidden border points
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;

        reset();
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
        updateTriangles();

        draggedPoint = null;
    }

    public ControlPoint getPoint(int pointX, int pointY) {
        return pointGrid[pointX][pointY];
    }

    public void beginDragging(MouseEvent e, int width, int height) {
        System.out.println("Clicked at: " + e.getPoint());
        for (ControlPoint[] controlPoints : pointGrid) {
            for (ControlPoint controlPoint : controlPoints) {
                var inBoundingBox = controlPoint.getBoundingBox().contains(e.getX(), e.getY());
                if (inBoundingBox) {
                    draggedPoint = controlPoint;
                    draggedPoint.beginDragging(e, width, height);
                    updateTriangles();
                    System.out.println("Dragging point: " + draggedPoint);
                }
            }
        }
    }

    public void doDragging(MouseEvent e, int width, int height) {
        if (draggedPoint != null) {
            draggedPoint.doDragging(e, width, height);
            updateTriangles();
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
        // As original list of triangles is updated concurrently, create a copy of list to prevent problems
        List<Polygon> copyOfTriangles = new ArrayList<>(pointTriangles);
        copyOfTriangles.forEach(triangle -> {
            var prevColor = g.getColor();
            g.setColor(Color.BLUE);
            g.drawPolygon(triangle);
            g.setColor(prevColor);
        });
        for (ControlPoint[] controlPoints : pointGrid) {
            for (ControlPoint controlPoint : controlPoints) {
                if (controlPoint.canDraw())
                    controlPoint.paintEntity(g);
            }
        }
    }

    @Override
    public void reset() {
        var gridWidth = pointGrid.length - 2;
        var gridHeight = pointGrid[0].length - 2;
        var widthInterval = panelWidth / (gridWidth + 1);
        var heightInterval = panelHeight / (gridHeight + 1);

        for (int i = 0; i < pointGrid.length; i++) {
            for (int j = 0; j < pointGrid[i].length; j++) {
                var xPos = widthInterval * i;
                var yPos = heightInterval * j;
                pointGrid[i][j] = new ControlPoint(xPos, yPos, !(i == 0 || j == 0 || i == pointGrid.length - 1 || j == pointGrid[0].length - 1));
            }
        }
        updateTriangles();
    }

    public void updateTriangles() {
        pointTriangles.clear();
        for (int i = 0; i < pointGrid.length; i++) {
            for (int j = 0; j < pointGrid[i].length; j++) {
                if (i > 0 && j > 0) {
                    var triangle = new Polygon();
                    triangle.addPoint(pointGrid[i - 1][j - 1].getPosition().x, pointGrid[i - 1][j - 1].getPosition().y);
                    triangle.addPoint(pointGrid[i - 1][j].getPosition().x, pointGrid[i - 1][j].getPosition().y);
                    triangle.addPoint(pointGrid[i][j].getPosition().x, pointGrid[i][j].getPosition().y);
                    pointTriangles.add(triangle);
                }
            }
        }
    }
}
