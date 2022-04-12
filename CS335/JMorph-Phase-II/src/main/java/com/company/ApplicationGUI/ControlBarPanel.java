package com.company.ApplicationGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final PreviewWindow parentFrame;

    private final JButton playPauseButton = new JButton("Play/Pause");
    private final JSlider keyFrameSlider;
    private boolean animating;


    ControlBarPanel(PreviewWindow frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        keyFrameSlider = new JSlider(0, parentFrame.getTotalFrames(), 0);
        add(playPauseButton);
        add(keyFrameSlider);

        establishListeners();
    }

    private void establishListeners() {
        playPauseButton.addActionListener(e -> parentFrame.togglePlay());
        keyFrameSlider.addChangeListener(this);
    }

    public void updateControlBar() {
        animating = true;
        keyFrameSlider.setValue(parentFrame.getCurrentFrame());
        animating = false;
        revalidate();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();

        if (!animating) {
            parentFrame.pause();
        }
        parentFrame.setFrame(slider.getValue());
        parentFrame.updateImage();

        revalidate();
    }
}
