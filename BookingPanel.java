import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class BookingPanel extends JPanel {

    private final AppointmentManager manager;

    private final JTextField studentIdField = new JTextField(22);
    private final JTextField nameField = new JTextField(22);
    private final JTextField gradeField = new JTextField(22);
    private final JComboBox<Appointment.ConcernType> concernBox =
            new JComboBox<>(Appointment.ConcernType.values());
    private final JComboBox<Counselor> counselorBox;
    private final JTextField dateField = new JTextField(22);
    private final JComboBox<String> timeBox;
    private final JButton bookButton = new JButton("Book appointment");
    private final JButton clearButton = new JButton("Clear form");

    public BookingPanel(AppointmentManager manager) {
        this.manager = manager;
        this.counselorBox = new JComboBox<>(manager.getCounselors().toArray(new Counselor[0]));
        this.timeBox = new JComboBox<>(timeSlotLabels());

        counselorBox.setRenderer(new PromptRenderer("Select a counselor"));
        concernBox.setRenderer(new PromptRenderer("Select a type of concern"));
        timeBox.setRenderer(new PromptRenderer("Select a time slot"));

        buildLayout();

        bookButton.addActionListener(e -> bookAppointment());
        clearButton.addActionListener(e -> clearForm());

        clearForm();
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());         

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("New consultation request"),
                BorderFactory.createEmptyBorder(8, 16, 12, 16)));

        int row = 0;
        addWide(form, row++, hint("Only a general type of concern is needed. "
                + "You do not have to explain your problem here."));
        addRow(form, row++, "Student ID", studentIdField);
        addRow(form, row++, "Full name", nameField);
        addRow(form, row++, "Grade or year and section", gradeField);
        addRow(form, row++, "Type of concern", concernBox);
        addRow(form, row++, "Counselor", counselorBox);
        addRow(form, row++, "Date (YYYY-MM-DD)", dateField);
        addRow(form, row++, "Time slot", timeBox);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.add(clearButton);
        buttons.add(bookButton);
        addWide(form, row++, buttons);

        addWide(form, row, hint("Consultations last one hour, Monday to Friday. "
                + "You can book up to " + AppointmentManager.MAX_DAYS_AHEAD + " days ahead."));

        add(form);
    }

    private void addRow(JPanel form, int row, String labelText, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.gridy = row;

        c.gridx = 0;
        c.anchor = GridBagConstraints.WEST;
        form.add(new JLabel(labelText), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
    }

    private void addWide(JPanel form, int row, JComponent component) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.gridx = 0;
        c.gridy = row;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(component, c);
    }

    private JLabel hint(String text) {
        JLabel label = new JLabel("<html><body style='width: 380px'>" + text + "</body></html>");
        label.setForeground(Color.GRAY);
        return label;
    }

    private static String[] timeSlotLabels() {
        String[] labels = new String[AppointmentManager.TIME_SLOTS.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = Appointment.formatTime(AppointmentManager.TIME_SLOTS.get(i));
        }
        return labels;
    }

    private void bookAppointment() {
        try {
            Student student = new Student(
                    studentIdField.getText(), nameField.getText(), gradeField.getText());
            Counselor counselor = (Counselor) counselorBox.getSelectedItem();
            Appointment.ConcernType concern = (Appointment.ConcernType) concernBox.getSelectedItem();
            LocalDate date = readDate();
            int slot = timeBox.getSelectedIndex();
            LocalTime time = (slot < 0) ? null : AppointmentManager.TIME_SLOTS.get(slot);
            Appointment booked = manager.bookAppointment(student, counselor, concern, date, time);
            Dialogs.info(this, "Appointment booked",
                    "Appointment #" + booked.getId() + " is booked.\n\n"
                    + "Student: " + student.getFullName() + "\n"
                    + "Counselor: " + counselor.getName() + "\n"
                    + "When: " + Appointment.formatLongDate(date) + ", " + Appointment.formatTime(time)
                    + "\n\nPlease arrive a few minutes early.");
            clearForm();

        } catch (IllegalArgumentException | IllegalStateException ex) {
            Dialogs.warning(this, ex.getMessage());
        } catch (IOException ex) {
            Dialogs.error(this, "The appointment was booked, but it could not be saved to the file:\n"
                    + ex.getMessage());
        }
    }

    private LocalDate readDate() {
        String text = dateField.getText().trim();
        if (text.isEmpty()) {
            return null;                        
        }
        try {
            return LocalDate.parse(text);        
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Enter a real date written as YYYY-MM-DD, for example "
                    + AppointmentManager.nextSchoolDay() + ".");
        }
    }

    private void clearForm() {
        studentIdField.setText("");
        nameField.setText("");
        gradeField.setText("");
        concernBox.setSelectedIndex(-1);
        counselorBox.setSelectedIndex(-1);
        timeBox.setSelectedIndex(-1);
        dateField.setText(AppointmentManager.nextSchoolDay().toString());
        studentIdField.requestFocusInWindow();
    }

    private static class PromptRenderer extends DefaultListCellRenderer {

        private final String prompt;

        PromptRenderer(String prompt) {
            this.prompt = prompt;
        }

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(
                    list, (value == null) ? prompt : value, index, isSelected, cellHasFocus);
            if (value == null) {
                c.setForeground(Color.GRAY);
            }
            return c;
        }
    }
}
