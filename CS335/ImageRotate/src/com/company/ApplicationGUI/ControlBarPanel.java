package com.company.ApplicationGUI;

import com.company.ApplicationGUI.ApplicationContext;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final ApplicationContext parentFrame;

    private final JSlider speedSlider = new JSlider(-360, 360, 0);

    ControlBarPanel(ApplicationContext frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        add(speedSlider);

        establishListeners();
    }

    private void establishListeners() {
        speedSlider.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();

        revalidate();
    }
}
