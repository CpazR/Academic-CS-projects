package GUI.ApplicationGUI;

import client.Client;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 800;
    public static final int panelHeight = 600;
    public static final Dimension windowSize = new Dimension(panelWidth, panelHeight);

    private final JPanel applicationPanel = new JPanel();
    private final ControlBarPanel controlPanel = new ControlBarPanel(this);
    private final JTerminal terminalPanel = new JTerminal(new Dimension(panelWidth - 50, panelHeight - 100));
    private final JFileChooser fileSelector = new JFileChooser("./");

    private final Client clientConnection;

    public ApplicationContext(String applicationName, Client client) {
        super(applicationName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);
        centerWindow();
        setPreferredSize(windowSize);
        setSize(windowSize);
        setResizable(false);

        clientConnection = client;

        // Initialize default file selector
        fileSelector.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                var isValid = false;

                if (f.isDirectory()) {
                    isValid = true;
                } else {
                    isValid = f.getName().toLowerCase().endsWith(".png") || f.getName().toLowerCase().endsWith(".jpg");
                }
                return isValid;
            }

            @Override
            public String getDescription() {
                return null;
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                if (client.isConnected()) {
                    disconnect();
                }
            }
        });
    }

    private void mainPanelSetup() {
        applicationPanel.setLayout(new BoxLayout(applicationPanel, BoxLayout.Y_AXIS));
        applicationPanel.add(controlPanel);
        applicationPanel.add(terminalPanel);
        add(applicationPanel);
        pack();
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    /// APPLICATION SPECIFIC FUNCTIONALITY
    public void connect() {
        // TODO: Add parameter window
        terminalPanel.emptyTerminal();
        clientConnection.initializeClient("localhost", 4444);
        revalidate();
    }

    public void disconnect() {
        clientConnection.closeClient();
        revalidate();
    }

    public void requestFileUpload() {
        try {
            clientConnection.uploadFile("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestFileDownload() {
        try {
            clientConnection.downloadFile("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

