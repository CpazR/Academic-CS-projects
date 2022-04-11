package GUI.ApplicationGUI;

import client.Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 800;
    public static final int panelHeight = 600;
    public static final Dimension windowSize = new Dimension(panelWidth, panelHeight);

    private final JPanel applicationPanel = new JPanel();
    private final ControlBarPanel controlPanel = new ControlBarPanel(this);
    private final JTerminal terminalPanel = new JTerminal(new Dimension(panelWidth - 50, panelHeight - 100));

    private final JFilePanel fileManagerPanel = new JFilePanel(this);

    private final JPanel connectionSetupPanel = new JPanel();
    private final JLabel addressLabel = new JLabel("Server IP:");
    private final JLabel portLabel = new JLabel("Server Port:");

    private final JTextField addressField = new JTextField("localhost", 12);
    private final JTextField portField = new JTextField("4444", 4);

    private String address = "localhost";
    private int port = 4444;

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

        addressField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                address = addressField.getText();
            }
        });
        portField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                port = Integer.parseInt(portField.getText());
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
    public boolean connect() {
        var connectedSuccessfully = false;
        connectionSetupPanel.setLayout(new BoxLayout(connectionSetupPanel, BoxLayout.Y_AXIS));
        connectionSetupPanel.add(addressLabel);
        connectionSetupPanel.add(addressField);
        connectionSetupPanel.add(portLabel);
        connectionSetupPanel.add(portField);

        var result = JOptionPane.showConfirmDialog(this, connectionSetupPanel, "Setup connection", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            terminalPanel.emptyTerminal();
            connectedSuccessfully = clientConnection.initializeClient(address, port);
            revalidate();
            SwingUtilities.updateComponentTreeUI(this);
        }
        return connectedSuccessfully;
    }

    public void disconnect() {
        clientConnection.closeClient();
        revalidate();
        SwingUtilities.updateComponentTreeUI(this);
    }

    public void requestFileUpload() {
        try {
            fileManagerPanel.updateFileList(clientConnection.showLocalFolderContents());
            var result = JOptionPane.showConfirmDialog(this, fileManagerPanel, "Upload File Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                clientConnection.uploadFile(fileManagerPanel.getSelectedFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestFileDownload() {
        try {
            fileManagerPanel.updateFileList(clientConnection.showRemoteFolderContents());
            var result = JOptionPane.showConfirmDialog(this, fileManagerPanel, "Download File Request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                clientConnection.downloadFile(fileManagerPanel.getSelectedFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestDeleteFile() {
        try {
            fileManagerPanel.updateFileList(clientConnection.showLocalFolderContents());
            var result = JOptionPane.showConfirmDialog(this, fileManagerPanel, "Delete File", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                clientConnection.deleteFile(fileManagerPanel.getSelectedFileName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

