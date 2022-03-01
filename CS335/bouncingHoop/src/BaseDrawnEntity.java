import java.awt.*;

/**
 * Future proofing projects that use primitives.
 */
public interface BaseDrawnEntity {
    /**
     * A delegate method that will be called by the application context each frame to draw the entity
     */
    void paintEntity(Graphics g);

    /**
     * Require a move method to be defined to each entity
     */
    void move();
}
