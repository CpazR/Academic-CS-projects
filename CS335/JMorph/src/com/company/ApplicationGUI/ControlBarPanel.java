package com.company.ApplicationGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final ApplicationContext parentFrame;

    private final JButton startNewMorphButton = new JButton("Start New Morph");
    private final JButton previewMorphButton = new JButton("Preview Morph");
    private final JSlider speedSlider = new JSlider(0, 10, 0);

    ControlBarPanel(ApplicationContext frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        add(startNewMorphButton);
        add(speedSlider);

        establishListeners();
    }

    private void establishListeners() {
        startNewMorphButton.addActionListener(e -> System.out.println("TODO: Open window for starting new morph"));
        speedSlider.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();
        parentFrame.setMorphState(slider.getValue());

        revalidate();
    }
}
