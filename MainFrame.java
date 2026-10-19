import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color HEADER_COLOR = new Color(0x1D4E4A);
    private static final Color HEADER_TEXT_COLOR = new Color(0xCFE6E2);

    public MainFrame(AppointmentManager manager) {
        super("Guidance Office Consultation Appointment System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1120, 660);
        setMinimumSize(new Dimension(1000, 560));
        setLocationRelativeTo(null);                   

        BookingPanel bookingPanel = new BookingPanel(manager);
        AppointmentsPanel appointmentsPanel = new AppointmentsPanel(manager);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Book appointment", bookingPanel);
        tabs.addTab("Appointments", appointmentsPanel);

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == appointmentsPanel) {
                appointmentsPanel.refresh();
            }
        });

        add(buildHeader(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(buildFooter(manager), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JLabel title = new JLabel("Guidance Office Consultation Appointment System");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));

        JLabel subtitle = new JLabel(
                "Book a consultation, check its status, and keep schedules free of conflicts.");
        subtitle.setForeground(HEADER_TEXT_COLOR);

        JPanel header = new JPanel(new BorderLayout(0, 4));
        header.setBackground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    private JLabel buildFooter(AppointmentManager manager) {
        JLabel footer = new JLabel("Records are saved automatically in: " + manager.getDataFilePath());
        footer.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return footer;
    }
}
