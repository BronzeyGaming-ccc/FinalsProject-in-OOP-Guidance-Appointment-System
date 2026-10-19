import javax.swing.JOptionPane;
import java.awt.Component;

public final class Dialogs {

    private Dialogs() {
    }

    public static void info(Component parent, String title, String message) {
        JOptionPane.showMessageDialog(parent, wrap(message), title, JOptionPane.INFORMATION_MESSAGE);
    }


    public static void warning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, wrap(message), "Please check", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, wrap(message), "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String title, String message) {
        int answer = JOptionPane.showConfirmDialog(parent, wrap(message), title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return answer == JOptionPane.YES_OPTION;
    }

    private static String wrap(String message) {
        String safe = String.valueOf(message)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
        return "<html><body style='width: 360px'>" + safe + "</body></html>";
    }
}
