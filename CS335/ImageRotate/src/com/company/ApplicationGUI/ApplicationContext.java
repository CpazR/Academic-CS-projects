package com.company.ApplicationGUI;

import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.RotatableImage;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 800;
    public static final int panelHeight = 600;

    private final JPanel applicationPanel = new JPanel();
    private final ControlBarPanel controlPanel = new ControlBarPanel(this);
    private final PrimitivePanel contentPanel = new PrimitivePanel(panelWidth, panelHeight);
    private final JFileChooser fileSelector = new JFileChooser("./");

    public ApplicationContext(String applicationName, List<BaseDrawnEntity> entityList) {
        super(applicationName);
        contentPanel.addEntities(entityList);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);
        contentPanel.initializeEntities();
        centerWindow();
        setResizable(false);

        // Initialize default file selector
        fileSelector.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                var isValid = false;

                if (f.isDirectory()) {
                    isValid = true;
                } else {
                    isValid = f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg");
                }
                return isValid;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
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

    private RotatableImage getImage() {
        return (RotatableImage) contentPanel.getEntity(0);
    }

    public void findImage() {
        if (fileSelector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            if (fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".png") || fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".jpg")) {
                contentPanel.removeEntity(getImage());
                contentPanel.addEntity(new RotatableImage(fileSelector.getSelectedFile().getPath()));
            } else {
                System.err.println("ERROR: File must be an PNG or JPG");
            }
        }

    }

    public void resetImage() {
        getImage().setAngle(0);
    }

    public void applyRotation(int value) {
        getImage().setAngle(value);
    }
}

