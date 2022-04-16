package com.company.ApplicationGUI;

import com.company.Entities.AnimatedGrid;
import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.ControlGrid;
import com.company.Entities.ControlImage;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.List;
import java.util.Properties;

public class ApplicationContext extends JFrame implements ChangeListener {

    public static final int panelWidth = 1600;
    public static final int panelHeight = 600;

    private int gridWidth = 5;
    private int gridHeight = 5;
    private int totalFrames = 60;

    private final JPanel applicationPanel = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JButton startNewMorphButton = new JButton("Start New Morph");
    private final JButton resetMorphButton = new JButton("Reset Morph");
    private final JButton previewMorphButton = new JButton("Preview / Render Morph");
    private final JPanel primitivePanelGroup = new JPanel();
    private final PrimitivePanel primitivePanelA = new PrimitivePanel(panelWidth / 2, panelHeight, true);
    private final PrimitivePanel primitivePanelB = new PrimitivePanel(panelWidth / 2, panelHeight, true);

    private final JPanel newMorphPanel = new JPanel();
    private final JLabel pictureALabel = new JLabel("Picture A:");
    private final JLabel pictureBLabel = new JLabel("Picture B:");
    private final JLabel gridWidthLabel = new JLabel("Width:");
    private final JLabel gridHeightLabel = new JLabel("Height:");
    private final JLabel morphFramesLabel = new JLabel("Frames for Morph:");

    private final JTextField gridWidthField = new JTextField("5", 3);
    private final JTextField gridHeightField = new JTextField("5", 3);
    private final JTextField morphFramesField = new JTextField("60", 3);
    private final JButton pictureAButton = new JButton("Add Image A");
    private final JButton pictureBButton = new JButton("Add Image B");
    private final JFileChooser fileSelector = new JFileChooser("./");
    private String selectedFileA;
    private String selectedFileB;

    private final JSlider brightnessSliderA = new JSlider(0, 20, 10);
    private final JSlider brightnessSliderB = new JSlider(0, 20, 10);

    public ApplicationContext(String applicationName, List<BaseDrawnEntity> entityList) {
        super(applicationName);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);

        establishListeners();

        // TODO: Replace with original images and parameterize as part of new morph
        primitivePanelA.addEntities(List.of(new ControlImage(this, "./assets/Bird1.jpg"), new ControlGrid(5, 5, primitivePanelA.getWidth(), primitivePanelA.getHeight())));
        primitivePanelB.addEntities(List.of(new ControlImage(this, "./assets/Bird2.jpg"), new ControlGrid(5, 5, primitivePanelB.getWidth(), primitivePanelB.getHeight())));

        centerWindow();
        setResizable(false);
    }

    public ApplicationContext(String applicationName) {
        this(applicationName, List.of());
    }

    private void mainPanelSetup() {
        primitivePanelGroup.add(primitivePanelA);
        primitivePanelGroup.add(primitivePanelB);
        controlPanel.add(brightnessSliderA);
        controlPanel.add(startNewMorphButton);
        controlPanel.add(resetMorphButton);
        controlPanel.add(previewMorphButton);
        controlPanel.add(brightnessSliderB);
        applicationPanel.setLayout(new BoxLayout(applicationPanel, BoxLayout.Y_AXIS));
        applicationPanel.add(controlPanel);
        applicationPanel.add(primitivePanelGroup);
        add(applicationPanel);
        pack();
    }

    private void establishListeners() {
        gridWidthField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                gridWidth = Integer.parseInt(gridWidthField.getText());

            }
        });
        gridHeightField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                gridHeight = Integer.parseInt(gridHeightField.getText());
            }
        });
        morphFramesField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                totalFrames = Integer.parseInt(morphFramesField.getText());
            }
        });
        startNewMorphButton.addActionListener(e -> {
            newMorphPanel.removeAll();
            newMorphPanel.setLayout(new BoxLayout(newMorphPanel, BoxLayout.Y_AXIS));
            newMorphPanel.add(pictureALabel);
            newMorphPanel.add(pictureAButton);
            newMorphPanel.add(pictureBLabel);
            newMorphPanel.add(pictureBButton);
            newMorphPanel.add(gridWidthLabel);
            newMorphPanel.add(gridWidthField);
            newMorphPanel.add(gridHeightLabel);
            newMorphPanel.add(gridHeightField);
            newMorphPanel.add(morphFramesLabel);
            newMorphPanel.add(morphFramesField);

            var result = JOptionPane.showConfirmDialog(this, newMorphPanel, "Custom game options", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                newMorph(gridWidth, gridHeight, totalFrames);
            }
        });
        previewMorphButton.addActionListener(e -> {
            var animatedGrid = new AnimatedGrid(List.of(primitivePanelA.getGrid(), primitivePanelB.getGrid()), totalFrames);
            new PreviewWindow(animatedGrid, List.of(primitivePanelA.getBufferedImage().clone(), primitivePanelB.getBufferedImage().clone()));
        });
        resetMorphButton.addActionListener(e -> {
            primitivePanelA.resetEntities();
            primitivePanelB.resetEntities();
        });

        pictureAButton.addActionListener(e -> {
            if (fileSelector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".png") || fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".jpg")) {
                    selectedFileA = fileSelector.getSelectedFile().getPath();
                } else {
                    System.err.println("ERROR: File must be an PNG or JPG");
                }
            }
        });

        pictureBButton.addActionListener(e -> {
            if (fileSelector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                if (fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".png") || fileSelector.getSelectedFile().getName().toLowerCase().endsWith(".jpg")) {
                    selectedFileB = fileSelector.getSelectedFile().getPath();
                } else {
                    System.err.println("ERROR: File must be an PNG or JPG");
                }
            }
        });

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

        brightnessSliderA.addChangeListener(this);
        brightnessSliderB.addChangeListener(this);
    }

    public void centerWindow() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - getHeight()) / 2);
        setLocation(x, y);
    }

    /// APPLICATION SPECIFIC FUNCTIONALITY

    public void newMorph(int gridWidth, int gridHeight, int totalFrames) {
        primitivePanelA.removeAllEntities();
        primitivePanelA.addEntities(List.of(new ControlImage(this, selectedFileA), new ControlGrid(gridWidth, gridHeight, primitivePanelA.getWidth(), primitivePanelA.getHeight())));
        primitivePanelB.removeAllEntities();
        primitivePanelB.addEntities(List.of(new ControlImage(this, selectedFileB), new ControlGrid(gridWidth, gridHeight, primitivePanelB.getWidth(), primitivePanelB.getHeight())));
        revalidate();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var slider = (JSlider) e.getSource();

        if (slider.equals(brightnessSliderA)) {
            primitivePanelA.getBufferedImage().setBrightness(slider.getValue());
        }
        if (slider.equals(brightnessSliderB)) {
            primitivePanelB.getBufferedImage().setBrightness(slider.getValue());
        }

        revalidate();
    }
}

