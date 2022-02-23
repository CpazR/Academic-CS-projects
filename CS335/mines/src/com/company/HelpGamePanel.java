package com.company;

import javax.swing.*;
import java.awt.*;

public class HelpGamePanel extends JPanel {

    HelpGamePanel() {
        setLayout(new GridLayout(0, 1));
        add(new JLabel("How to play:"));
        add(new JLabel("Once a new game is begun, you can click on any area where only then will the bombs be planted.\n" +
                "Spaces adjacent to any bombs will have a number rather than simply being blank. This number indicated how many bombs the surrounding spaces contain.\n" +
                "\n"));
        add(new JLabel("To Win:"));
        add(new JLabel("Expose all non-bomb areas."));
        add(new JLabel("To Lose:"));
        add(new JLabel("Once a bomb is clicked, the whole board will be exposed.\nAt which point a new game can be begun.\n"));
    }
}
