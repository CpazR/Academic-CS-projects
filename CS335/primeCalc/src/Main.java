import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class Main {

    public static void main(String[] args) {
        var sieve = new PrimeSieve(1000000);
        var application = new CalculatorApplicationContext(sieve);
    }
}

class CalculatorApplicationContext extends JFrame implements ActionListener {

    private final PrimeSieve SIEVE;
    private final String INPUT_FIELD_HINT = "Input a number!";

    private final JTextField inputField = new JTextField(INPUT_FIELD_HINT);
    private final JButton inputButton = new JButton("Submit");
    private final JTextArea outputField = new JTextArea("Enter a number between 2 - 1,000,000");

    CalculatorApplicationContext(PrimeSieve sieve) {
        this.SIEVE = sieve;
        windowSetup();
    }

    private void windowSetup() {
        this.setTitle("Prime Number Calculator");
        this.getRootPane().setDefaultButton(inputButton);
        this.setLayout(null);
        this.setSize(330, 430);
        this.setVisible(true);

        // Set up the input field
        inputField.setSize(120, 20);
        inputField.setLocation(getPercentagePosition(inputField, 50, 30));
        this.add(inputField);

        inputButton.setSize(100, 40);
        inputButton.setLocation(getPercentagePosition(inputButton, 50));
        this.add(inputButton);

        // Set up the output field
        outputField.setSize(getWidth(), 100);
        outputField.setLocation(getPercentagePosition(outputField, 50, 85));
        outputField.setEditable(false);
        this.add(outputField);

        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        establishListeners();
        this.revalidate();
    }

    /**
     * Setup listeners for any components that might need them
     */
    private void establishListeners() {
        // Triggers when button is clicked
        inputButton.addActionListener(actionEvent -> {
            var inputValue = 0;
            var outputText = "";
            try {
                inputValue = Integer.parseInt(inputField.getText().trim());
                outputText = SIEVE.lookForPrimeMessage(inputValue);
            } catch (NumberFormatException numberFormatException) {
                outputText = "Invalid input: Only accepts integers";
            }
            outputField.setText(outputText);
        });

        // Trigger and hide/show hint when focusing on field
        inputField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputField.getText().equals(INPUT_FIELD_HINT)) {
                    inputField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (inputField.getText().equals("")) {
                    inputField.setText(INPUT_FIELD_HINT);
                }
            }
        });
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

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}