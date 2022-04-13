package com.company.ApplicationGUI;

import com.company.Entities.AnimatedGrid;
import com.company.Entities.ControlImage;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.company.ApplicationGUI.ApplicationContext.panelHeight;
import static com.company.ApplicationGUI.ApplicationContext.panelWidth;

public class PreviewWindow extends JFrame {

    private final JPanel previewPanel = new JPanel();
    private final ControlBarPanel previewControlPanel;
    private final PrimitivePanel previewAnimatedPanel = new PrimitivePanel(panelWidth / 2, panelHeight, false);
    private final AnimatedGrid animatedGrid;
    private final ControlImage imageBufferA;
    private final ControlImage imageBufferB;

    private int currentFrame;
    private final int totalFrames;
    private boolean paused;

    // A thread that will run at roughly a fixed rate
    private ScheduledExecutorService animatorThread;

    PreviewWindow(AnimatedGrid animatedGrid, List<ControlImage> images) {
        this.animatedGrid = animatedGrid;
        totalFrames = animatedGrid.getTotalFrames();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewControlPanel = new ControlBarPanel(this);
        imageBufferA = images.get(1);
        imageBufferB = images.get(0);
        previewPanel.add(previewControlPanel);
        previewPanel.add(previewAnimatedPanel);
        previewAnimatedPanel.addEntity(imageBufferA);
        previewAnimatedPanel.addEntity(imageBufferB);
        add(previewPanel);
        setVisible(true);
        setResizable(false);
        pack();

        // Start/pause thread with button press
        scheduleAnimationThread();
    }

    private void scheduleAnimationThread() {
        animatorThread = Executors.newSingleThreadScheduledExecutor();
        animatorThread.scheduleAtFixedRate(() -> {
            if (currentFrame <= totalFrames) {
                currentFrame++;
                previewControlPanel.updateControlBar();
                updateImage();
                if (!animatedGrid.animate(currentFrame) || !this.isVisible()) {
                    System.out.println("Animation stopped, shutting down thread.");
                }
            } else {
                pause();
            }
        }, 0L, 15, TimeUnit.MILLISECONDS);
    }

    public void updateImage() {
        // Account for any potential floating point errors
        var alphaValue = Math.min(1f, Math.max(0f, (float) currentFrame / (float) totalFrames));
        imageBufferA.setAlpha(alphaValue);
        imageBufferB.setAlpha(1f - alphaValue);

        var initialTriangles = animatedGrid.getKeyframes().get(0).getPointTriangles();
        var activeTriangles = animatedGrid.getActiveFrame().getPointTriangles();
        var destinationTriangles = animatedGrid.getKeyframes().get(1).getPointTriangles();
        for (int x = 0; x < activeTriangles.length; x++) {
            for (int y = 0; y < activeTriangles[0].length; y++) {
                imageBufferB.morph(initialTriangles[x][y], activeTriangles[x][y]);
                imageBufferA.morph(activeTriangles[x][y], destinationTriangles[x][y]);
            }
        }
    }

    public void togglePlay() {
        paused = !paused;
        if (paused) {
            animatorThread.shutdown();
        } else {
            // Restart animation if finished
            currentFrame = currentFrame >= totalFrames ? 0 : currentFrame;
            scheduleAnimationThread();
        }
    }

    public void pause() {
        paused = true;
        animatorThread.shutdown();
    }

    public void setFrame(int newFrame) {
        animatedGrid.animate(newFrame);
        currentFrame = newFrame;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    /**
     * Perform cleanup on any running threads
     */
    @Override
    public void dispose() {
        super.dispose();
        animatorThread.shutdown();
    }
}
