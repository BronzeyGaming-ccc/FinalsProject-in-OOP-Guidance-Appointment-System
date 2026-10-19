import java.util.Locale;

public class Student {

    private final String studentId;
    private final String fullName;
    private final String gradeSection;

    public Student(String studentId, String fullName, String gradeSection) {
        this.studentId = checkStudentId(studentId);
        this.fullName = checkFullName(fullName);
        this.gradeSection = checkGradeSection(gradeSection);
    }

    public String getStudentId() {
        return studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getGradeSection() {
        return gradeSection;
    }

    private static String checkStudentId(String value) {
        String id = tidy(value);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Enter the student ID.");
        }
        if (!id.matches("[A-Za-z0-9-]{4,15}")) {
            throw new IllegalArgumentException("The student ID should be 4 to 15 characters, "
                    + "using only letters, numbers, or hyphens (for example 2026-0123).");
        }
        return id.toUpperCase(Locale.ROOT);
    }

    private static String checkFullName(String value) {
        String name = tidy(value);
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Enter the student's full name.");
        }
        if (name.length() < 2 || name.length() > 50 || !name.matches("\\p{L}[\\p{L} .'-]*")) {
            throw new IllegalArgumentException("The full name should be 2 to 50 characters, "
                    + "using only letters, spaces, periods, hyphens, or apostrophes.");
        }
        return name;
    }

    private static String checkGradeSection(String value) {
        String text = tidy(value);
        if (text.isEmpty()) {
            throw new IllegalArgumentException(
                    "Enter the grade or year and section (for example Grade 10 - Newton).");
        }
        if (!text.matches("[\\p{L}\\p{N} .#/'-]{2,30}")) {
            throw new IllegalArgumentException("The grade or year and section should be 2 to 30 characters, "
                    + "using only letters, numbers, spaces, or the symbols - . # / '");
        }
        return text;
    }

    private static String tidy(String value) {
        return (value == null) ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
