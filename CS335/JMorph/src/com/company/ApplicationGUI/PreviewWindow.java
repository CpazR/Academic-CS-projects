package com.company.ApplicationGUI;

import com.company.Entities.AnimatedGrid;

import javax.swing.*;
import java.util.List;

import static com.company.ApplicationGUI.ApplicationContext.panelHeight;
import static com.company.ApplicationGUI.ApplicationContext.panelWidth;

public class PreviewWindow extends JFrame {

    private final JPanel previewPanel = new JPanel();
    private final ControlBarPanel previewControlPanel = new ControlBarPanel(this);
    private final PrimitivePanel previewAnimatedPanel = new PrimitivePanel(panelWidth / 2, panelHeight);

    private final Thread animatorThread;

    PreviewWindow(AnimatedGrid animatedGrid) {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.Y_AXIS));
        previewPanel.add(previewControlPanel);
        previewPanel.add(previewAnimatedPanel);
        previewAnimatedPanel.addEntity(animatedGrid);
        add(previewPanel);
        setVisible(true);
        setResizable(false);
        pack();

        // Start/pause thread with button press
        animatorThread = new Thread(() -> {
            System.out.println("Thread running...");
            animatedGrid.animate();
        });
    }
}
