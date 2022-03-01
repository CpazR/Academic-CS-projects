import java.util.List;

public class BouncingHoop {
    public static void main(String[] args) {
        var hoop = new Hoop(true);
        new ApplicationContext(List.of(hoop));
    }
}
