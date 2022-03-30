package GUI.ApplicationGUI;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JTerminal extends JPanel {

    private final JTextArea outputArea = new JTextArea("Click \"Connect to server\" to start transferring files.\n");

    JTerminal(Dimension size) {
        PrintStream outputStream = new PrintStream(new CaptureStream(this, System.out));
        System.setOut(outputStream);
        System.setErr(outputStream);
        setBorder(new TitledBorder(new EtchedBorder(), "Client Terminal"));
        outputArea.setEditable(false);

        var scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(size);
        add(scrollPane);
    }

    public void appendText(String text) {
        outputArea.append(text);
        outputArea.setCaretPosition(outputArea.getText().length());
    }

    public void emptyTerminal() {
        outputArea.setText("");
    }
}

class CaptureStream extends OutputStream {

    private final JTerminal outputConsumer;
    private final PrintStream originalStream;

    CaptureStream(JTerminal consumer, PrintStream originalStream) {
        outputConsumer = consumer;
        this.originalStream = originalStream;
    }

    @Override
    public void write(int b) throws IOException {
        char currentChar = (char) b;
        outputConsumer.appendText(Character.toString(currentChar));
        originalStream.print(currentChar);
    }
}
