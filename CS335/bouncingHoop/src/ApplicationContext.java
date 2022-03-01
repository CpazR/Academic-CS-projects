import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ApplicationContext extends JFrame {

    static final int panelWidth = 800;
    static final int panelHeight = 800;
    static double applicationSpeed = 10.0;

    ApplicationContext(List<BaseDrawnEntity> entityList) {
        // TODO: make new class (subclass?) for this.
        var controlPanel = new JPanel();
        var contentPane = new PrimitivePanel();
        contentPane.addEntities(entityList);
        JFrame frame = new JFrame("Dancing Hoop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(panelWidth, panelHeight);
        frame.setContentPane(contentPane);
        frame.setVisible(true);
        contentPane.initializeEntities();
        pack();
    }

    /**
     * A panel dedicated to drawing primitives
     */
    class PrimitivePanel extends JPanel {
        // Primary list of entities to be drawn. Maintains order through a linked list.
        private final LinkedList<BaseDrawnEntity> drawableEntities = new LinkedList<>();
        private final int threadDelay = 10;

        /**
         * Setup threads to process entities
         */
        public void initializeEntities() {
            drawableEntities.forEach(entity -> {
                // Create a thread to update the animation and repaint
                Thread thread = new Thread(() -> {
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
                thread.start();
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

}

