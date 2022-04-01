package com.company.Entities;

import java.awt.*;

/**
 * Future proofing projects that use primitives.
 */
public interface BaseDrawnEntity {
    /**
     * A delegate method that will be called by the application context each frame to draw the entity
     */
    void paintEntity(Graphics g);

    void reset();
}
