import ApplicationGUI.ApplicationContext;

import java.util.List;

public class Hoop {
    public static void main(String[] args) {
        var hoop = new Entities.Hoop();
        new ApplicationContext(List.of(hoop));
    }
}
