
import java.awt.*;
import javax.swing.*;
 
public class BouncingBall extends JPanel {
  private Ball b;
  private int delay=10;
  private double speed=10.0;
  private static int panelWidth=800;
  private static int panelHeight=800;

  public BouncingBall() {
    // Set up the bouncing ball with random speeds and colors
    b = new Ball(panelWidth,panelHeight, 
                (float)(speed*Math.random()), 
                (float)(speed*Math.random()), 
        new Color((float)Math.random(), (float)Math.random(), (float)Math.random()));

    // Create a thread to update the animation and repaint
    Thread thread = new Thread() {
      public void run() {
        while (true) {
          // Ask the ball to move itself and then repaint
          b.moveBall();
          repaint();
          try {
            Thread.sleep(delay);
          } catch (InterruptedException ex) { }
        }
      }
    };
    thread.start();
  }
 
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    // Draw the ball
    b.paintBall(g);
  }
 
  public static void main(String[] args) {
//    JFrame.setDefaultLookAndFeelDecorated(true);
    JFrame frame = new JFrame("Bouncing Ball");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(panelWidth, panelHeight);
    frame.setContentPane(new BouncingBall());
    frame.setVisible(true);
  }
}
