package com.company.ApplicationGUI;

import com.company.Entities.AnimatedGrid;

import javax.swing.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.company.ApplicationGUI.ApplicationContext.panelHeight;
import static com.company.ApplicationGUI.ApplicationContext.panelWidth;

public class PreviewWindow extends JFrame {

    private final JPanel previewPanel = new JPanel();
    private final ControlBarPanel previewControlPanel = new ControlBarPanel(this);
    private final PrimitivePanel previewAnimatedPanel = new PrimitivePanel(panelWidth / 2, panelHeight, false);
    private final AnimatedGrid animatedGrid;

    private int currentFrame;
    private final int totalFrames;
    private boolean paused;

    // A thread that will run at roughly a fixed rate
    private ScheduledExecutorService animatorThread;

    PreviewWindow(AnimatedGrid animatedGrid) {
        this.animatedGrid = animatedGrid;
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(previewControlPanel);
        previewPanel.add(previewAnimatedPanel);
        previewAnimatedPanel.addEntity(animatedGrid);
        add(previewPanel);
        setVisible(true);
        setResizable(false);
        pack();

        totalFrames = animatedGrid.getTotalFrames();
        // Start/pause thread with button press
        scheduleAnimationThread();
    }

    private void scheduleAnimationThread() {
        animatorThread = Executors.newSingleThreadScheduledExecutor();
        animatorThread.scheduleAtFixedRate(() -> {
            currentFrame++;
            previewControlPanel.updateControlBar();
            if (currentFrame <= totalFrames) {
                if (!animatedGrid.animate(currentFrame) || !this.isVisible()) {
                    System.out.println("Animation stopped, shutting down thread.");
                }
            } else {
                pause();
            }
        }, 0L, 15, TimeUnit.MILLISECONDS);
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
