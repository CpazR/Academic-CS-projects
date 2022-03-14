package ApplicationGUI;

import Entities.BaseDrawnEntity;
import Entities.Hoop;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 800;
    public static final int panelHeight = 600;
    public static double applicationSpeed = 10.0;

    private final JPanel applicationPanel = new JPanel();
    private final ControlBarPanel controlPanel = new ControlBarPanel(this);
    private final PrimitivePanel contentPanel = new PrimitivePanel(panelWidth, panelHeight);

    public ApplicationContext(List<BaseDrawnEntity> entityList) {
        super("Dancing Hoop");
        contentPanel.addEntities(entityList);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);
        contentPanel.initializeEntities();
        centerWindow();
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
        private int threadDelay = 10;

        PrimitivePanel(int width, int height) {
            setPreferredSize(new Dimension(width, height));
        }

        /**
         * Setup threads to process entities
         */
        public void initializeEntities() {
            drawableEntities.forEach(entity -> {
                // Create a thread to update the animation and repaint
                var processingThread = new Thread(() -> {
                    while (true) {
                        // Ask the entity to move itself and then repaint
                        entity.move();
                        repaint();
                        try {
                            Thread.sleep(threadDelay);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                processingThread.start();
            });
        }

        public void addEntities(List<BaseDrawnEntity> entity) {
            drawableEntities.addAll(entity);
        }

        public void addEntity(BaseDrawnEntity entity) {
            drawableEntities.add(entity);
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawableEntities.forEach(baseDrawnEntity -> baseDrawnEntity.paintEntity(g));
        }
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    /// APPLICATION SPECIFIC FUNCTIONALITY

    public void togglePoints() {
        contentPanel.drawableEntities.forEach(entity -> {
            if (entity instanceof Hoop) {
                ((Hoop) entity).togglePoints();
            }
        });
    }

    public void toggleCurve() {
        contentPanel.drawableEntities.forEach(entity -> {
            if (entity instanceof Hoop) {
                ((Hoop) entity).toggleCurve();
            }
        });
    }

    public void setThreadDelay(int newValue) {
        contentPanel.threadDelay = 50 - newValue;
    }
}

