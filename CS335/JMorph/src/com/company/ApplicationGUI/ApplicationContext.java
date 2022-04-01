package com.company.ApplicationGUI;

import com.company.Entities.BaseDrawnEntity;
import com.company.Entities.ControlGrid;
import com.company.Entities.ControlImage;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ApplicationContext extends JFrame {

    public static final int panelWidth = 1600;
    public static final int panelHeight = 600;

    private ControlImage controlImage;

    private final JPanel applicationPanel = new JPanel();
    private final JPanel controlPanel = new JPanel();
    private final JButton startNewMorphButton = new JButton("Start New Morph");
    private final JButton resetMorphButton = new JButton("Reset Morph");
    private final JButton previewMorphButton = new JButton("Preview Morph");
    private final JPanel primitivePanelGroup = new JPanel();
    private final PrimitivePanel primitivePanelA = new PrimitivePanel(panelWidth / 2, panelHeight);
    private final PrimitivePanel primitivePanelB = new PrimitivePanel(panelWidth / 2, panelHeight);

    private final JPanel previewPanel = new JPanel();
    private final ControlBarPanel previewControlPanel = new ControlBarPanel(this);
    private final PrimitivePanel previewAnimatedPanel = new PrimitivePanel(panelWidth, panelHeight);


    public ApplicationContext(String applicationName, List<BaseDrawnEntity> entityList) {
        super(applicationName);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainPanelSetup();
        setVisible(true);

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

    public void setMorphState(int newMorphState) {

    }
}

