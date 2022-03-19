package com.company;

import com.company.Entities.BaseDrawnEntity;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;

public class RotatableImage implements BaseDrawnEntity {

    private BufferedImage image;

    private double angle;

    RotatableImage(String imageLocation) {
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
        var affineTransform = new AffineTransform();

        affineTransform.translate(image.getWidth() / 2, image.getHeight() / 2);
        affineTransform.rotate(Math.toRadians(angle));
        affineTransform.scale(0.5, 0.5);
        affineTransform.translate(-image.getWidth() / 2, -image.getHeight() / 2);

        var graphics2d = (Graphics2D) g;
        graphics2d.drawImage(image, affineTransform, null);
    }

    @Override
    public void move() {

    }
}
