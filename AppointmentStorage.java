import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentStorage {

    private static final String HEADER =
            "id,studentId,fullName,gradeSection,counselor,date,time,concern,status";

    private final Path file;

    public AppointmentStorage(String fileName) {
        this.file = Paths.get(fileName);
    }

    public String getFilePath() {
        return file.toAbsolutePath().toString();
    }

    public void save(List<Appointment> appointments) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(HEADER);
        for (Appointment a : appointments) {
            lines.add(String.join(",",
                    String.valueOf(a.getId()),
                    a.getStudent().getStudentId(),
                    a.getStudent().getFullName(),
                    a.getStudent().getGradeSection(),
                    a.getCounselor().getName(),
                    a.getDate().toString(),
                    a.getTime().toString(),
                    a.getConcern().name(),
                    a.getStatus().name()));
        }
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    public List<Appointment> load(List<Counselor> knownCounselors) throws IOException {
        List<Appointment> appointments = new ArrayList<>();
        if (!Files.exists(file)) {
            return appointments;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {          
            String line = lines.get(i).trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                appointments.add(parseLine(line, knownCounselors));
            } catch (RuntimeException ex) {
                System.err.println("Skipped line " + (i + 1) + " of "
                        + file.getFileName() + ": " + ex.getMessage());
            }
        }
        return appointments;
    }

    private Appointment parseLine(String line, List<Counselor> knownCounselors) {
        String[] parts = line.split(",", -1);
        if (parts.length != 9) {
            throw new IllegalArgumentException("expected 9 values but found " + parts.length);
        }
        int id = Integer.parseInt(parts[0].trim());
        Student student = new Student(parts[1], parts[2], parts[3]);
        Counselor counselor = findCounselor(parts[4].trim(), knownCounselors);
        LocalDate date = LocalDate.parse(parts[5].trim());
        LocalTime time = LocalTime.parse(parts[6].trim());
        Appointment.ConcernType concern = Appointment.ConcernType.valueOf(parts[7].trim());
        Appointment.Status status = Appointment.Status.valueOf(parts[8].trim());
        return new Appointment(id, student, counselor, date, time, concern, status);
    }

    private Counselor findCounselor(String name, List<Counselor> knownCounselors) {
        for (Counselor c : knownCounselors) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return new Counselor(name, "");
    }
}
