package com.company.Entities;

import com.company.ApplicationGUI.ApplicationContext;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;

public class ControlImage implements BaseDrawnEntity {

    private BufferedImage image;

    private double angle;

    public ControlImage(String imageLocation) {
        try {
            image = ImageIO.read(new File(imageLocation));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public void paintEntity(Graphics g) {
        var rotationalAffineTransform = new AffineTransform();

        var xCenter = ApplicationContext.panelWidth / 2;
        var yCenter = ApplicationContext.panelHeight / 2;

        var imageHorCenter = image.getWidth() / 2;
        var imageVerCenter = image.getHeight() / 2;

        // Center transformation at center of screen
        rotationalAffineTransform.translate(xCenter - imageHorCenter, yCenter - imageVerCenter);

        // Apply transformations to rotate around center of image
        rotationalAffineTransform.translate(imageHorCenter, imageVerCenter);
        // Apply rotation
        rotationalAffineTransform.rotate(Math.toRadians(angle));
        // Scale down image if too large (apply while image is centered to keep things aligned)
        if (image.getWidth() > ApplicationContext.panelWidth && image.getHeight() > ApplicationContext.panelHeight) {
            rotationalAffineTransform.scale(0.3, 0.3);
        }
        rotationalAffineTransform.translate(-imageHorCenter, -imageVerCenter);

        // Draw image with transformation
        ((Graphics2D) g).drawImage(image, rotationalAffineTransform, null);
    }

    @Override
    public void move() {

    }
}
