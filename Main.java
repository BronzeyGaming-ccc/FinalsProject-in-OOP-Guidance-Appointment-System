import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.io.IOException;
import java.util.Collections;

public class Main {


    private static final String DATA_FILE = "appointments.csv";
    public static void main(String[] args) {

        SwingUtilities.invokeLater(Main::start);
    }

    private static void start() {
        useNativeLookAndFeel();
        makeFontsLarger(14f);

        try {
            AppointmentStorage storage = new AppointmentStorage(DATA_FILE);
            AppointmentManager manager = new AppointmentManager(storage);
            new MainFrame(manager).setVisible(true);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null,
                    "The appointment file could not be read:\n" + ex.getMessage()
                    + "\n\nFix or delete " + DATA_FILE + " and start the program again.",
                    "Cannot start", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void useNativeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            
        }
    }

    private static void makeFontsLarger(float size) {
        for (Object key : Collections.list(UIManager.getDefaults().keys())) {
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, new FontUIResource(((FontUIResource) value).deriveFont(size)));
            }
        }
    }
}
