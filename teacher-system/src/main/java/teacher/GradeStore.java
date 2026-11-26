package teacher;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Saves entered scores to ~/.teacher-system/grades.properties so they survive restarts. */
final class GradeStore {
    private final Properties props = new Properties();
    private final Path file = Path.of(System.getProperty("user.home"), ".teacher-system", "grades.properties");

    GradeStore() {
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException ignored) { }
        }
    }

    static String key(Section section, Student student, Period period) {
        return section.subject().code() + "|" + section.code() + "|" + student.id() + "|" + period.name();
    }

    /** 10 raw strings (score,max x 5 components) or null when never saved. */
    String[] load(String key) {
        String v = props.getProperty(key);
        if (v == null) return null;
        String[] a = v.split(",", -1);
        return a.length == 10 ? a : null;
    }

    void save(String key, String[] raw) {
        props.setProperty(key, String.join(",", raw));
        try {
            Files.createDirectories(file.getParent());
            try (OutputStream out = Files.newOutputStream(file)) {
                props.store(out, "Teacher System grades");
            }
        } catch (IOException ignored) { }
    }
}
