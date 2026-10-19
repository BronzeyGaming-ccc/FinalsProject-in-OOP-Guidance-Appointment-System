public class Counselor {

    private final String name;
    private final String specialty;

    public Counselor(String name, String specialty) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("A counselor needs a name.");
        }
        this.name = name.trim();
        this.specialty = (specialty == null) ? "" : specialty.trim();
    }

    public String getName() {
        return name;
    }

    public String getSpecialty() {
        return specialty;
    }

    @Override
    public String toString() {
        return specialty.isEmpty() ? name : name + " (" + specialty + ")";
    }
}
