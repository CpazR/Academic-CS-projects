package ApplicationGUI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class ControlBarPanel extends JPanel implements ChangeListener {

    private final ApplicationContext parentFrame;

    private final JButton pointToggleButton = new JButton("Toggle Points");
    private final JButton toggleCurveButton = new JButton("Toggle Curve");
    private final JSlider speedSlider = new JSlider(1, 49, 39);

    ControlBarPanel(ApplicationContext frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));
        add(pointToggleButton);
        add(speedSlider);
        add(toggleCurveButton);

        establishListeners();
    }

    private void establishListeners() {
        pointToggleButton.addActionListener(e -> parentFrame.togglePoints());
        toggleCurveButton.addActionListener(e -> parentFrame.toggleCurve());
        speedSlider.addChangeListener(this);
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();
        parentFrame.setThreadDelay(slider.getValue());

        revalidate();
    }
}
