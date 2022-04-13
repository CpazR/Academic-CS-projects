package com.company.Entities;

import com.company.ApplicationGUI.ApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ControlImage implements BaseDrawnEntity, Cloneable {

    private final JFrame context;
    private final BufferedImage imageBuffer;
    private BufferedImage imageBufferWarped;
    private final Image image;
    private final int panelWidth = ApplicationContext.panelWidth / 2;
    private final int panelHeight = ApplicationContext.panelHeight;
    private boolean imageLoaded = false;
    private float alpha = 1f;


    public ControlImage(JFrame context, String imageURL) {
        Image imageTemp;

        this.context = context;
        // The ImageObserver implementation to observe loading of the image
        try {
            var imageFile = new File(imageURL);
            if (imageFile.exists()) {
                imageTemp = ImageIO.read(imageFile).getScaledInstance(panelWidth, panelHeight, Image.SCALE_DEFAULT);
            } else {
                throw new IOException("Cannot find file at " + imageURL);
            }
        } catch (IOException e) {
            e.printStackTrace();
            imageTemp = null;
        }

        image = imageTemp;

        imageBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        assert image != null;
        imageBuffer.getGraphics().drawImage(image, 0, 0, panelWidth, panelHeight, 0, 0, image.getWidth(null),
                image.getHeight(null), null);
    }

    public void setAlpha(float newAlpha) {
        alpha = newAlpha;
    }

    public BufferedImage getImageBuffer() {
        return imageBuffer;
    }

    public void morph(Triangle triS, Triangle triD) {
        // Apply morph between images with animation frame
        if (imageBufferWarped == null)
            imageBufferWarped = new BufferedImage(imageBuffer.getWidth(context), imageBuffer.getHeight(context),
                    BufferedImage.TYPE_INT_RGB);
        MorphTools.warpTriangle(imageBuffer, imageBufferWarped, triS, triD, null, null, false);
    }

    @Override
    public void paintEntity(Graphics g) {
        var g2d = (Graphics2D) g;
        var alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(alphaComposite);
        if (imageBufferWarped == null) {
            g2d.drawImage(imageBuffer, 0, 0, context);
        } else {
            g2d.drawImage(imageBufferWarped, 0, 0, context);
        }
    }

    @Override
    public void reset() {

    }

    @Override
    public ControlImage clone() {
        try {
            ControlImage clone = (ControlImage) super.clone();
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
