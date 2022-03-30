package GUI.ApplicationGUI;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class JFilePanel extends JPanel {
    private final ApplicationContext context;
    private String selectedFileName;
    private final Set<String> files = new HashSet<>();
    private final JComboBox<String> fileSelectorDropdown = new JComboBox<>();

    JFilePanel(ApplicationContext context) {
        setLayout(new GridLayout(0, 1));

        this.context = context;
        add(fileSelectorDropdown);
    }

    public void updateFileList(String[] filesNames) {
        files.clear();
        files.addAll(Set.of(filesNames));
        files.forEach(fileSelectorDropdown::addItem);
    }

    public String getSelectedFileName() {
        selectedFileName = fileSelectorDropdown.getSelectedItem().toString();
        System.out.println(selectedFileName);
        return selectedFileName;
    }
}
