package GUI.ApplicationGUI;

import javax.swing.*;
import java.awt.*;

public class ControlBarPanel extends JPanel {

    private final ApplicationContext parentFrame;

    private final JButton connectButton = new JButton("Connect to server");

    private final JButton uploadButton = new JButton("Upload File");
    private final JButton downloadButton = new JButton("Download File");
    private final JButton deleteButton = new JButton("Delete File");
    private final JButton disconnectButton = new JButton("Disconnect from server");

    ControlBarPanel(ApplicationContext frame) {
        parentFrame = frame;
        setLayout(new GridLayout(1, 0));

        add(connectButton);
        establishListeners();
    }

    private void connect() {
        if (parentFrame.connect()) {
            remove(connectButton);

            add(uploadButton);
            add(downloadButton);
            add(deleteButton);
            add(disconnectButton);
            revalidate();
        }
    }

    private void disconnect() {
        parentFrame.disconnect();
        remove(uploadButton);
        remove(downloadButton);
        remove(deleteButton);
        remove(disconnectButton);

        add(connectButton);
        revalidate();
    }

    private void establishListeners() {
        connectButton.addActionListener(e -> connect());
        uploadButton.addActionListener(e -> parentFrame.requestFileUpload());
        downloadButton.addActionListener(e -> parentFrame.requestFileDownload());
        deleteButton.addActionListener(e -> parentFrame.requestDeleteFile());
        disconnectButton.addActionListener(e -> disconnect());
    }
}
