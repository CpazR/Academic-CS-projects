package com.company.ApplicationGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final JFrame parentFrame;

    private final JButton playPauseButton = new JButton("Play/Pause");
    private final JButton restartButton = new JButton("Restart");
    private final JSlider keyFrameSlider = new JSlider(0, 60, 0);

    ControlBarPanel(JFrame frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        add(playPauseButton);
        add(keyFrameSlider);

        establishListeners();
    }

    private void establishListeners() {
        playPauseButton.addActionListener(e -> System.out.println("TODO: Open window for starting new morph"));
        keyFrameSlider.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();

        revalidate();
    }
}
