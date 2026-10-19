import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppointmentManager {

    public static final int MAX_DAYS_AHEAD = 30;
    public static final List<LocalTime> TIME_SLOTS = Collections.unmodifiableList(Arrays.asList(
            LocalTime.of(8, 0), LocalTime.of(9, 0), LocalTime.of(10, 0), LocalTime.of(11, 0),
            LocalTime.of(13, 0), LocalTime.of(14, 0), LocalTime.of(15, 0)));

    private final AppointmentStorage storage;
    private final List<Counselor> counselors = new ArrayList<>();
    private final List<Appointment> appointments;
    private int nextId = 1;

    public AppointmentManager(AppointmentStorage storage) throws IOException {
        this.storage = storage;

        counselors.add(new Counselor("Ms. Reyes", "Academic guidance"));
        counselors.add(new Counselor("Mr. Santos", "Career guidance"));
        counselors.add(new Counselor("Ms. Cruz", "Personal and social"));

        this.appointments = storage.load(counselors);
        for (Appointment a : appointments) {
            nextId = Math.max(nextId, a.getId() + 1);
        }
        sortAppointments();
    }

    public Appointment bookAppointment(Student student, Counselor counselor,
                                       Appointment.ConcernType concern,
                                       LocalDate date, LocalTime time) throws IOException {
        require(student != null, "Enter the student's details.");
        require(counselor != null, "Select a counselor.");
        require(concern != null, "Select the type of concern.");
        require(date != null, "Enter the appointment date as YYYY-MM-DD.");
        require(time != null, "Select a time slot.");
        checkDateAndTime(date, time);
        checkConflicts(student, counselor, date, time);
        Appointment appointment = new Appointment(nextId++, student, counselor, date, time, concern);
        appointments.add(appointment);
        sortAppointments();
        storage.save(appointments);
        return appointment;
    }

    private void checkDateAndTime(LocalDate date, LocalTime time) {
        LocalDate today = LocalDate.now();
        LocalDate lastDay = today.plusDays(MAX_DAYS_AHEAD);

        if (date.isBefore(today)) {
            throw new IllegalArgumentException(
                    "That date has already passed. Choose today or a later date.");
        }
        if (date.isAfter(lastDay)) {
            throw new IllegalArgumentException("Appointments can be booked up to " + MAX_DAYS_AHEAD
                    + " days ahead. Choose a date on or before " + Appointment.formatLongDate(lastDay) + ".");
        }
        if (isWeekend(date)) {
            throw new IllegalArgumentException(
                    "The Guidance Office is closed on weekends. Choose a date from Monday to Friday.");
        }
        if (!TIME_SLOTS.contains(time)) {
            throw new IllegalArgumentException("Select one of the available time slots.");
        }
        if (date.equals(today) && !time.isAfter(LocalTime.now())) {
            throw new IllegalArgumentException(
                    "That time has already passed today. Choose a later time or another date.");
        }
    }

    private void checkConflicts(Student student, Counselor counselor, LocalDate date, LocalTime time) {
        for (Appointment existing : appointments) {
            if (existing.isBookedWith(counselor, date, time)) {
                throw new IllegalStateException(counselor.getName() + " is already booked on "
                        + Appointment.formatLongDate(date) + " at " + Appointment.formatTime(time)
                        + ". Choose another time or another counselor.");
            }
            if (existing.isBookedBy(student, date, time)) {
                throw new IllegalStateException("Student " + student.getStudentId()
                        + " already has an appointment on " + Appointment.formatLongDate(date)
                        + " at " + Appointment.formatTime(time) + ". Choose a different time.");
            }
        }
    }

    public void markCompleted(int id) throws IOException {
        findById(id).markCompleted();
        storage.save(appointments);
    }

    public void cancelAppointment(int id) throws IOException {
        findById(id).cancel();
        storage.save(appointments);
    }

    public Appointment findById(int id) {
        for (Appointment a : appointments) {
            if (a.getId() == id) {
                return a;
            }
        }
        throw new IllegalArgumentException("Appointment #" + id + " was not found.");
    }

    public List<Appointment> search(String keyword, Appointment.Status statusFilter) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment a : appointments) {
            boolean statusMatches = (statusFilter == null) || a.getStatus() == statusFilter;
            if (statusMatches && a.matches(keyword)) {
                result.add(a);
            }
        }
        return result;
    }

    public int getTotalCount() {
        return appointments.size();
    }

    public int countByStatus(Appointment.Status status) {
        int count = 0;
        for (Appointment a : appointments) {
            if (a.getStatus() == status) {
                count++;
            }
        }
        return count;
    }

    public List<Counselor> getCounselors() {
        return Collections.unmodifiableList(counselors);
    }

    public String getDataFilePath() {
        return storage.getFilePath();
    }

    public static LocalDate nextSchoolDay() {
        LocalDate day = LocalDate.now().plusDays(1);
        while (isWeekend(day)) {
            day = day.plusDays(1);
        }
        return day;
    }

    private static boolean isWeekend(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    private void sortAppointments() {
        appointments.sort(Comparator.comparing(Appointment::getDate)
                .thenComparing(Appointment::getTime)
                .thenComparingInt(Appointment::getId));
    }
}
