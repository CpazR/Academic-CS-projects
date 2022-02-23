package com.company;

import javax.swing.*;
import java.awt.*;

public class HelpGamePanel extends JPanel {

    HelpGamePanel() {
        setLayout(new GridLayout(0, 1));
        add(new JLabel("How to play:"));
        add(new JLabel("<html>Once a new game is begun, you can click on any area where only then will the bombs be planted. <br/>" +
                "Spaces adjacent to any bombs will have a number rather than simply being blank. This number indicated how many bombs the surrounding spaces contain.<br/>" +
                "Right clicking on a space flags that space. Once a space is flagged, it cannot be exposed until it's been un-flagged.<br/>" +
                "Flagging is useful for marking spaces assumed to be bombs.</html>"));
        add(new JLabel("To Win:"));
        add(new JLabel("Expose all non-bomb areas."));
        add(new JLabel("To Lose:"));
        add(new JLabel("<html>Once a bomb is clicked, the whole board will be exposed.<br/>At which point a new game can be begun.<br/></html>"));
    }
}
