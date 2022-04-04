package com.company.ApplicationGUI;

import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.ControlGrid;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.LinkedList;
import java.util.List;

/**
 * A panel dedicated to drawing primitives
 */
public class PrimitivePanel extends JPanel {
    // Primary list of entities to be drawn. Maintains order through a linked list.
    private final LinkedList<BaseDrawnEntity> drawableEntities = new LinkedList<>();

    public PrimitivePanel(int width, int height, boolean useMouse) {
        setPreferredSize(new Dimension(width, height));
        setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

        if (useMouse) {
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    getGrid().beginDragging(e);
                }

                public void mouseReleased(MouseEvent e) {
                    getGrid().endDragging(e);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    getGrid().doDragging(e, getWidth(), getHeight());
                }
            });
        }
    }

    /**
     * Setup threads to process entities
     */
    public void initializeEntities() {
        // TODO: check if entities require any initialization
    }

    public BaseDrawnEntity getEntity(int index) {
        BaseDrawnEntity entity = null;
        if (drawableEntities.size() > 0) {
            entity = drawableEntities.get(index);
        } else {
            System.err.println("ERROR: No entities exist.");
        }
        return entity;
    }

    public void addEntities(List<BaseDrawnEntity> entity) {
        drawableEntities.addAll(entity);
    }

    public void addEntity(BaseDrawnEntity entity) {
        drawableEntities.add(entity);
    }

    public void removeEntity(Object entity) {
        if (drawableEntities.contains(entity)) {
            drawableEntities.remove(entity);
            System.out.println("INFO: Successfully removed entity.");
        } else {
            System.out.println("INFO: Attempted to remove an entity that does not exist.");
        }
    }

    public void removeAllEntities() {
        if (!drawableEntities.isEmpty()) {
            drawableEntities.clear();
            System.out.println("INFO: Successfully removed entities.");
        } else {
            System.out.println("INFO: No drawable entities to remove.");
        }
    }

    public void resetEntities() {
        drawableEntities.forEach(BaseDrawnEntity::reset);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawableEntities.forEach(baseDrawnEntity -> baseDrawnEntity.paintEntity(g));
        repaint();
    }

    public ControlGrid getGrid() {
        return (ControlGrid) drawableEntities.get(0);
    }

}
