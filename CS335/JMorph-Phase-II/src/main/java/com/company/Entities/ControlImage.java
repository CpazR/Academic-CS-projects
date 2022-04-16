package com.company.Entities;

import com.company.ApplicationGUI.ApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

public class ControlImage implements BaseDrawnEntity, Cloneable {

    private final JFrame context;
    private final BufferedImage originalBuffer;
    private BufferedImage imageBuffer;
    private BufferedImage imageBufferWarped;
    private float alpha = 1f;
    private int brightness = 10;

    public ControlImage(JFrame context, String imageURL) {
        Image imageTemp;

        this.context = context;
        // The ImageObserver implementation to observe loading of the image
        int panelHeight = ApplicationContext.panelHeight;
        int panelWidth = ApplicationContext.panelWidth / 2;
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

        Image image = imageTemp;

        originalBuffer = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_ARGB);
        assert image != null;
        originalBuffer.getGraphics().drawImage(image, 0, 0, panelWidth, panelHeight, 0, 0, image.getWidth(null), image.getHeight(null), null);

        imageBuffer = new BufferedImage(originalBuffer.getColorModel(), originalBuffer.copyData(null), originalBuffer.isAlphaPremultiplied(), null);
    }

    public void setAlpha(float newAlpha) {
        alpha = newAlpha;
    }

    public void setBrightness(int newBrightness) {
        System.out.println("Updating brightness");
        if (newBrightness < 1) {
            newBrightness = 1;
        }
        imageBuffer = new BufferedImage(originalBuffer.getColorModel(), originalBuffer.copyData(null), originalBuffer.isAlphaPremultiplied(), null);
        brightness = newBrightness;
        var rescaleOp = new RescaleOp((float) brightness / 10f, 15, null);
        rescaleOp.filter(imageBuffer, imageBuffer);
//        for (int i = 0; i < imageBuffer.getWidth(); i++) {
//            for (int j = 0; j < imageBuffer.getHeight(); j++) {
//                var rgb = imageBuffer.getRaster().getPixel(i, j, new int[3]);
//
//                imageBuffer.getRaster().setPixel(i, j, new int[]{rgb[0] + newBrightness, rgb[1] + newBrightness, rgb[2] + newBrightness});
//
//            }
//
//        }
    }

    public BufferedImage getImageBuffer() {
        return imageBuffer;
    }

    public BufferedImage getWarpedImageBuffer() {
        return imageBufferWarped;
    }

    public void morph(Triangle triS, Triangle triD) {
        // Apply morph between images with animation frame
        if (imageBufferWarped == null)
            imageBufferWarped = new BufferedImage(imageBuffer.getWidth(context), imageBuffer.getHeight(context), BufferedImage.TYPE_INT_ARGB);

        MorphTools.warpTriangle(imageBuffer, imageBufferWarped, triS, triD, alpha, null, null, false);
    }

    @Override
    public void paintEntity(Graphics g) {
        var g2d = (Graphics2D) g;
        if (imageBufferWarped == null) {
            // Render image like normal in primitive pane
            g2d.drawImage(imageBuffer, 0, 0, context);
        } else {
            // Render morphed image, probably in preview pane
            g2d.drawImage(imageBufferWarped, 0, 0, context);
        }
    }

    @Override
    public void reset() {
        alpha = 1f;
        brightness = 10;
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

    public float getAlpha() {
        return alpha;
    }
}
