package com.company.ApplicationGUI;

import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.ControlImage;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 800;
    public static final int panelHeight = 600;

    private ControlImage controlImage;

    private final JPanel applicationPanel = new JPanel();
    private final ControlBarPanel controlPanel = new ControlBarPanel(this);
    private final PrimitivePanel contentPanel = new PrimitivePanel(panelWidth, panelHeight);

    public ApplicationContext(String applicationName, List<BaseDrawnEntity> entityList) {
        super(applicationName);
        contentPanel.addEntities(entityList);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);
        contentPanel.initializeEntities();
        centerWindow();
        setResizable(false);


    }

    public ApplicationContext(String applicationName) {
        this(applicationName, List.of());
    }

    private void mainPanelSetup() {
        applicationPanel.setLayout(new BoxLayout(applicationPanel, BoxLayout.Y_AXIS));
        applicationPanel.add(controlPanel);
        applicationPanel.add(contentPanel);
        add(applicationPanel);
        pack();
    }

    /**
     * A panel dedicated to drawing primitives
     */
    class PrimitivePanel extends JPanel {
        // Primary list of entities to be drawn. Maintains order through a linked list.
        private final LinkedList<BaseDrawnEntity> drawableEntities = new LinkedList<>();

        PrimitivePanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
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

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawableEntities.forEach(baseDrawnEntity -> baseDrawnEntity.paintEntity(g));
            repaint();
        }
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    /// APPLICATION SPECIFIC FUNCTIONALITY

    public void setMorphState(int newMorphState) {

    }
}

