import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class AppointmentsPanel extends JPanel {

    private static final String[] COLUMNS = {
            "No.", "Date", "Time", "Student ID", "Student name",
            "Grade / section", "Counselor", "Concern", "Status"};
    private static final int[] COLUMN_WIDTHS = {50, 100, 85, 110, 190, 140, 120, 140, 100};
    private static final int STATUS_COLUMN = 8;

    private static final Color SCHEDULED_COLOR = new Color(0x1E5AA8);
    private static final Color COMPLETED_COLOR = new Color(0x2E7D32);
    private static final Color CANCELLED_COLOR = new Color(0xB23A3A);

    private final AppointmentManager manager;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField searchField = new JTextField(18);
    private final JButton searchButton = new JButton("Search");
    private final JButton showAllButton = new JButton("Show all");
    private final JComboBox<String> statusFilterBox;

    private final JButton completeButton = new JButton("Mark as completed");
    private final JButton cancelButton = new JButton("Cancel appointment");
    private final JLabel summaryLabel = new JLabel(" ");

    public AppointmentsPanel(AppointmentManager manager) {
        this.manager = manager;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        table.getColumnModel().getColumn(STATUS_COLUMN).setCellRenderer(new StatusRenderer());

        statusFilterBox = new JComboBox<>(statusFilterLabels());

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        refresh();
    }

    private JPanel buildSearchBar() {
        searchField.setToolTipText(
                "Type a name, student ID, counselor, or date (YYYY-MM-DD), then press Enter");

        searchField.addActionListener(e -> refresh());           
        searchButton.addActionListener(e -> refresh());
        statusFilterBox.addActionListener(e -> refresh());
        showAllButton.addActionListener(e -> {
            searchField.setText("");
            statusFilterBox.setSelectedIndex(0);
            refresh();
        });

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bar.add(new JLabel("Search"));
        bar.add(searchField);
        bar.add(searchButton);
        bar.add(new JLabel("Status"));
        bar.add(statusFilterBox);
        bar.add(showAllButton);
        return bar;
    }

    private JPanel buildBottomBar() {
        completeButton.addActionListener(e -> completeSelected());
        cancelButton.addActionListener(e -> cancelSelected());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(completeButton);
        buttons.add(cancelButton);

        JPanel bar = new JPanel(new BorderLayout());
        bar.add(buttons, BorderLayout.WEST);
        bar.add(summaryLabel, BorderLayout.EAST);
        return bar;
    }

    public void refresh() {
        List<Appointment> rows = manager.search(searchField.getText(), selectedStatusFilter());

        tableModel.setRowCount(0);
        for (Appointment a : rows) {
            tableModel.addRow(new Object[] {
                    a.getId(),
                    a.getDate(),
                    Appointment.formatTime(a.getTime()),
                    a.getStudent().getStudentId(),
                    a.getStudent().getFullName(),
                    a.getStudent().getGradeSection(),
                    a.getCounselor().getName(),
                    a.getConcern(),
                    a.getStatus()});
        }

        if (manager.getTotalCount() == 0) {
            summaryLabel.setText("No appointments yet. Use the Book appointment tab to add one.");
        } else if (rows.isEmpty()) {
            summaryLabel.setText("No appointments match your search.");
        } else {
            summaryLabel.setText("Showing " + rows.size() + " of " + manager.getTotalCount()
                    + "     Scheduled: " + manager.countByStatus(Appointment.Status.SCHEDULED)
                    + "     Completed: " + manager.countByStatus(Appointment.Status.COMPLETED)
                    + "     Cancelled: " + manager.countByStatus(Appointment.Status.CANCELLED));
        }
    }

    private static String[] statusFilterLabels() {
        Appointment.Status[] statuses = Appointment.Status.values();
        String[] labels = new String[statuses.length + 1];
        labels[0] = "All";
        for (int i = 0; i < statuses.length; i++) {
            labels[i + 1] = statuses[i].toString();
        }
        return labels;
    }

    private Appointment.Status selectedStatusFilter() {
        int index = statusFilterBox.getSelectedIndex();
        return (index <= 0) ? null : Appointment.Status.values()[index - 1];
    }

    private Appointment getSelectedScheduledAppointment() {
        int row = table.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "No appointment selected", "Select an appointment in the table first.");
            return null;
        }
        int id = (Integer) tableModel.getValueAt(row, 0);
        Appointment appointment = manager.findById(id);
        if (!appointment.isScheduled()) {
            Dialogs.info(this, "Cannot change this appointment",
                    "Appointment #" + id + " is already " + appointment.getStatus()
                    + ". Only scheduled appointments can be changed.");
            return null;
        }
        return appointment;
    }

    private void completeSelected() {
        Appointment appointment = getSelectedScheduledAppointment();
        if (appointment == null) {
            return;
        }
        boolean yes = Dialogs.confirm(this, "Mark as completed",
                "Mark appointment #" + appointment.getId() + " for "
                + appointment.getStudent().getFullName() + " as completed?\n\nThis cannot be undone.");
        if (!yes) {
            return;
        }
        try {
            manager.markCompleted(appointment.getId());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Dialogs.warning(this, ex.getMessage());
        } catch (IOException ex) {
            Dialogs.error(this, "The change could not be saved to the file:\n" + ex.getMessage());
        }
        refresh();
    }

    private void cancelSelected() {
        Appointment appointment = getSelectedScheduledAppointment();
        if (appointment == null) {
            return;
        }
        boolean yes = Dialogs.confirm(this, "Cancel appointment",
                "Cancel appointment #" + appointment.getId() + " for "
                + appointment.getStudent().getFullName() + " on " + appointment.getDate()
                + " at " + Appointment.formatTime(appointment.getTime())
                + "?\n\nThe time slot becomes free again. This cannot be undone.");
        if (!yes) {
            return;
        }
        try {
            manager.cancelAppointment(appointment.getId());
        } catch (IllegalArgumentException | IllegalStateException ex) {
            Dialogs.warning(this, ex.getMessage());
        } catch (IOException ex) {
            Dialogs.error(this, "The change could not be saved to the file:\n" + ex.getMessage());
        }
        refresh();
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {                        
                if (value == Appointment.Status.COMPLETED) {
                    c.setForeground(COMPLETED_COLOR);
                } else if (value == Appointment.Status.CANCELLED) {
                    c.setForeground(CANCELLED_COLOR);
                } else {
                    c.setForeground(SCHEDULED_COLOR);
                }
            }
            return c;
        }
    }
}
