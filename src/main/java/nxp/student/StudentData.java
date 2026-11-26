package nxp.student;

import nxp.core.User;
import java.util.List;

/** Student demo data. Replace with a database/service later. */
public final class StudentData {
    private StudentData() {}
    public static final String[] SEM_NAMES = {"First Semester", "Second Semester"};
    public static final List<List<Subj>> SEM = List.of(
        List.of(new Subj("IT-100", "Introduction to Computing", 90, 96), new Subj("IT-101", "Computer Programming", 93, 93),
                new Subj("GEC1", "Understanding the Self", 92, 92), new Subj("GEC2", "Readings in Philippine History", 89, 89),
                new Subj("GEC3", "Mathematics in the Modern World", 88, 94), new Subj("PATHFit1", "Movement of Competency Training", 90, 96)),
        List.of(new Subj("IT-102", "Computer Programming 2", null, null), new Subj("IT-103", "Data Structures and Algorithms", null, null),
                new Subj("IT-104", "Discrete Mathematics", null, null), new Subj("GEC4", "Purposive Communication", null, null),
                new Subj("GEC5", "Art Appreciation", null, null), new Subj("GE ELEC 7", "Environmental Science", null, null),
                new Subj("PATHFit 2", "Exercise-based Fitness Activities", null, null)));

    public static StudentProfile profileFor(User u) {
        return new StudentProfile("Mario A. Mimay", "Mario", "26-1071", u.email(), "Bachelor of Science in information technology", "1st Year - Block B", "BSIT - First Year - Block B");
    }
}
