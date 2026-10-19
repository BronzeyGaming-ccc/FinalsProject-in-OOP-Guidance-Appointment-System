import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Appointment {

    public enum Status {
        SCHEDULED("Scheduled"),
        COMPLETED("Completed"),
        CANCELLED("Cancelled");

        private final String label;

        Status(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public enum ConcernType {
        ACADEMIC("Academic"),
        PERSONAL("Personal or social"),
        CAREER("Career"),
        OTHER("Other");

        private final String label;

        ConcernType(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter LONG_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH);

    private final int id;
    private final Student student;
    private final Counselor counselor;
    private final LocalDate date;
    private final LocalTime time;
    private final ConcernType concern;
    private Status status;

    public Appointment(int id, Student student, Counselor counselor,
                       LocalDate date, LocalTime time, ConcernType concern) {
        this(id, student, counselor, date, time, concern, Status.SCHEDULED);
    }

    public Appointment(int id, Student student, Counselor counselor,
                       LocalDate date, LocalTime time, ConcernType concern, Status status) {
        if (student == null || counselor == null || date == null
                || time == null || concern == null || status == null) {
            throw new IllegalArgumentException(
                    "An appointment needs a student, counselor, date, time, concern, and status.");
        }
        this.id = id;
        this.student = student;
        this.counselor = counselor;
        this.date = date;
        this.time = time;
        this.concern = concern;
        this.status = status;
    }

    public boolean isScheduled() {
        return status == Status.SCHEDULED;
    }

    public void markCompleted() {
        requireScheduled();
        status = Status.COMPLETED;
    }

    public void cancel() {
        requireScheduled();
        status = Status.CANCELLED;
    }

    private void requireScheduled() {
        if (status != Status.SCHEDULED) {
            throw new IllegalStateException("Appointment #" + id + " is already " + status
                    + ". Only scheduled appointments can be changed.");
        }
    }

    public boolean isBookedWith(Counselor otherCounselor, LocalDate otherDate, LocalTime otherTime) {
        return isScheduled()
                && counselor.getName().equalsIgnoreCase(otherCounselor.getName())
                && date.equals(otherDate)
                && time.equals(otherTime);
    }

    public boolean isBookedBy(Student otherStudent, LocalDate otherDate, LocalTime otherTime) {
        return isScheduled()
                && student.getStudentId().equals(otherStudent.getStudentId())
                && date.equals(otherDate)
                && time.equals(otherTime);
    }

    public boolean matches(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return true;
        }
        String details = student.getStudentId() + " " + student.getFullName() + " "
                + student.getGradeSection() + " " + counselor.getName() + " "
                + date + " " + formatTime(time) + " " + concern + " " + status;
        return details.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }

    public static String formatTime(LocalTime value) {
        return value.format(TIME_FORMAT);
    }

    public static String formatLongDate(LocalDate value) {
        return value.format(LONG_DATE_FORMAT);
    }

    public int getId() {
        return id;
    }

    public Student getStudent() {
        return student;
    }

    public Counselor getCounselor() {
        return counselor;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public ConcernType getConcern() {
        return concern;
    }

    public Status getStatus() {
        return status;
    }
}
