package com.company;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class CustomGamePanel extends JPanel implements ChangeListener {

    private final JSlider gridRowSlider = new JSlider(9, 24, 9);
    private final JSlider gridColSlider = new JSlider(9, 30, 9);
    private int totalSize = 9 * 9;
    private final JSlider bombCountSlider = new JSlider((int) (totalSize * .1), (int) (totalSize * .25), 10);

    private final JLabel rowLabel = new JLabel("Height: " + getRowValue());
    private final JLabel colLabel = new JLabel("Width: " + getColValue());
    private final JLabel bombCountLabel = new JLabel("Bombs: " + getBombCountValue());

    CustomGamePanel() {
        setLayout(new GridLayout(0, 1));

        gridColSlider.addChangeListener(this);
        gridRowSlider.addChangeListener(this);
        bombCountSlider.addChangeListener(this);

        add(colLabel);
        add(gridColSlider);
        add(rowLabel);
        add(gridRowSlider);
        add(bombCountLabel);
        add(bombCountSlider);
    }

    public int getRowValue() {
        return gridRowSlider.getValue();
    }

    public int getColValue() {
        return gridColSlider.getValue();
    }

    public int getBombCountValue() {
        return bombCountSlider.getValue();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        var changedSlider = (JSlider) e.getSource();

        if (changedSlider.equals(gridRowSlider)) {
            rowLabel.setText("Rows: " + getRowValue());
        }

        if (changedSlider.equals(gridColSlider)) {
            colLabel.setText("Columns: " + getColValue());
        }

        if (changedSlider.equals(bombCountSlider)) {
            bombCountLabel.setText("Bombs: " + getBombCountValue());
        }

        totalSize = getColValue() * getRowValue();

        bombCountSlider.setMinimum((int) (totalSize * .1));
        bombCountSlider.setMaximum((int) (totalSize * .25));

        revalidate();
    }
}
