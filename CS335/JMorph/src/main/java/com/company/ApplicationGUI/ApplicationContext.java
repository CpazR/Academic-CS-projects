package com.company.ApplicationGUI;

import com.company.Entities.AnimatedGrid;
import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.ControlGrid;
import com.company.Entities.ControlPoint;
import netscape.javascript.JSObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 1600;
    public static final int panelHeight = 600;

    private int gridWidth = 5;
    private int gridHeight = 5;
    private int totalFrames = 60;

    private final JPanel applicationPanel = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JButton startNewMorphButton = new JButton("Start New Morph");
    private final JButton resetMorphButton = new JButton("Reset Morph");
    private final JButton previewMorphButton = new JButton("Preview Morph");
    private final JButton saveMorphButton = new JButton("Save Morph");
    private final JButton loadMorphButton = new JButton("Load Morph");
    private final JPanel primitivePanelGroup = new JPanel();
    private final PrimitivePanel primitivePanelA = new PrimitivePanel(panelWidth / 2, panelHeight, true);
    private final PrimitivePanel primitivePanelB = new PrimitivePanel(panelWidth / 2, panelHeight, true);

    private final JPanel newMorphPanel = new JPanel();
    private final JLabel gridWidthLabel = new JLabel("Width:");
    private final JLabel gridHeightLabel = new JLabel("Height:");
    private final JLabel morphFramesLabel = new JLabel("Frames for Morph:");

    private final JTextField gridWidthField = new JTextField("5", 3);
    private final JTextField gridHeightField = new JTextField("5", 3);
    private final JTextField morphFramesField = new JTextField("60", 3);

    public ApplicationContext(String applicationName, List<BaseDrawnEntity> entityList) {
        super(applicationName);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);

        establishListeners();

        primitivePanelA.addEntities(List.of(new ControlGrid(5, 5, primitivePanelA.getWidth(), primitivePanelA.getHeight())));
        primitivePanelB.addEntities(List.of(new ControlGrid(5, 5, primitivePanelB.getWidth(), primitivePanelB.getHeight())));

        centerWindow();
        setResizable(false);
    }

    public ApplicationContext(String applicationName) {
        this(applicationName, List.of());
    }

    private void mainPanelSetup() {
        primitivePanelGroup.add(primitivePanelA);
        primitivePanelGroup.add(primitivePanelB);
        controlPanel.add(startNewMorphButton);
        controlPanel.add(resetMorphButton);
        controlPanel.add(previewMorphButton);
        applicationPanel.setLayout(new BoxLayout(applicationPanel, BoxLayout.Y_AXIS));
        applicationPanel.add(controlPanel);
        applicationPanel.add(primitivePanelGroup);
        add(applicationPanel);
        pack();
    }

    private void establishListeners() {
        startNewMorphButton.addActionListener(e -> {
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

            newMorphPanel.removeAll();
            newMorphPanel.setLayout(new BoxLayout(newMorphPanel, BoxLayout.Y_AXIS));
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
            new PreviewWindow(animatedGrid);
        });
        resetMorphButton.addActionListener(e -> {
            primitivePanelA.resetEntities();
            primitivePanelB.resetEntities();
        });
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
        primitivePanelA.addEntities(List.of(new ControlGrid(gridWidth, gridHeight, primitivePanelA.getWidth(), primitivePanelA.getHeight())));
        primitivePanelB.removeAllEntities();
        primitivePanelB.addEntities(List.of(new ControlGrid(gridWidth, gridHeight, primitivePanelB.getWidth(), primitivePanelB.getHeight())));
        revalidate();
    }

    public void saveMorph() {
        // Save morph as JSON
        // Save grid width and height
        // Save grids array of coordinates
        // Save total number of frames for morph
        var pointMapA = new JSONArray();
        var pointMapB = new JSONArray();

        var gridA = primitivePanelA.getGrid();

        for (int i = 0; i < gridA.getGridOfPoints().length; i++) {
            pointMapA.addAll(i, Arrays.stream(gridA.getGridOfPoints()[i]).collect(Collectors.toSet()));
        }

        var gridB = primitivePanelB.getGrid();

        for (int i = 0; i < gridB.getGridOfPoints().length; i++) {
            pointMapB.addAll(i, Arrays.stream(gridB.getGridOfPoints()[i]).collect(Collectors.toSet()));
        }

        var jsonObject = new JSONObject();

        jsonObject.put(gridWidth, "Morph grid width");
        jsonObject.put(gridHeight, "Morph grid height");
        jsonObject.put(totalFrames, "Morph frames");
        jsonObject.put(pointMapA, "Grid A points");
        jsonObject.put(pointMapB, "Grid B points");

        try {
            var jsonFile = new FileWriter("./morphSave.json");
            jsonFile.write(jsonObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadMorph() {
        var jsonObject = new JSONObject();
        try {
            var jsonFile = new FileReader("./morphSave.json");
            jsonObject = (JSONObject) new JSONParser().parse(jsonFile);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}

