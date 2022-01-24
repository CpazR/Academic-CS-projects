import com.sun.jdi.IntegerValue;

import javax.swing.*;
import java.awt.*;

public class Main {

    public static void main(String[] args) {
        var sieve = new PrimeSieve(1000000);
        var application = new CalculatorApplicationContext();

    }
}

class CalculatorApplicationContext extends JFrame {
    private final JTextField inputField = new JTextField("This is the input!");
    private final JButton inputButton = new JButton("Submit");
    private final JTextArea outputField = new JTextArea("This is the output!");

    CalculatorApplicationContext() {
        windowSetup();
    }

    private void windowSetup() {
        this.setLayout(null);
        this.setSize(300, 500);
        this.setVisible(true);
        // Set up the header

        // Set up the input field
        inputField.setSize(120, 30);
        inputField.setLocation(getPercentagePosition(inputField, 50, 30));
        inputField.setAlignmentX(JTextField.CENTER_ALIGNMENT);
        inputField.setAlignmentY(JTextField.BOTTOM_ALIGNMENT);
        this.add(inputField);

        inputButton.setSize(100, 50);
        inputButton.setLocation(getPercentagePosition(inputButton, 50));
        inputButton.setAlignmentX(JButton.CENTER_ALIGNMENT);
        inputButton.setAlignmentY(JButton.BOTTOM_ALIGNMENT);
        this.add(inputButton);

        // Set up the output field
        outputField.setSize(100, 50);
        outputField.setLocation(getPercentagePosition(outputField, 50, 80));
        outputField.setAlignmentX(JTextArea.CENTER_ALIGNMENT);
        outputField.setAlignmentY(JTextArea.BOTTOM_ALIGNMENT);
        this.add(outputField);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.revalidate();
    }

    /**
     * Provided a component and a percentage (value from 0 - 100), return a point the component is centered around
     */
    private Point getPercentagePosition(JComponent component, int percentage) {
        return getPercentagePosition(component, percentage, percentage);
    }

    private Point getPercentagePosition(JComponent component, int percentageWidth, int percentageHeight) {
        double calculatedPercentageWidth = (double) percentageWidth / 100;
        double calculatedPercentageHeight = (double) percentageHeight / 100;
        return new Point(Double.valueOf(getWidth() * calculatedPercentageWidth - (component.getWidth() * calculatedPercentageWidth)).intValue(), Double.valueOf(getHeight() * calculatedPercentageHeight - (component.getHeight() * calculatedPercentageHeight)).intValue());
    }
}