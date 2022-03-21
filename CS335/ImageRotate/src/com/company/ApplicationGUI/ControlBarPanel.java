package com.company.ApplicationGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final ApplicationContext parentFrame;

    private final JButton imageButton = new JButton("Image");
    private final JButton resetButton = new JButton("Reset");

    private final JSlider speedSlider = new JSlider(-360, 360, 0);

    ControlBarPanel(ApplicationContext frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        add(imageButton);
        add(speedSlider);
        add(resetButton);

        establishListeners();
    }

    private void establishListeners() {
        imageButton.addActionListener(e -> parentFrame.findImage());
        resetButton.addActionListener(e -> {
            parentFrame.resetImage();
            speedSlider.setValue(0);
        });

        speedSlider.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();
        parentFrame.applyRotation(slider.getValue());

        revalidate();
    }
}
