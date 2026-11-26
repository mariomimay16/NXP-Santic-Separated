package teacher;

import java.util.ArrayList;
import java.util.List;

/** Sample data taken from the prototype. Replace with a database later. */
final class DataStore {
    private DataStore() {}

    private static final List<Subject> SUBJECTS = List.of(
            new Subject("IT-101", "Computer Programming 1", 1, Semester.FIRST),
            new Subject("IT-108", "Operating System", 2, Semester.FIRST),
            new Subject("IT-115", "Networking 1", 3, Semester.FIRST),
            new Subject("IT-118", "Internet of Things", 3, Semester.FIRST),
            new Subject("IT-124", "Social and Professional Issues in Computing", 4, Semester.FIRST),
            new Subject("IT-102", "Computer Programming 2", 1, Semester.SECOND),
            new Subject("IT-109", "Fundamental of Database Systems", 2, Semester.SECOND),
            new Subject("IT-120", "Networking 2", 3, Semester.SECOND));

    private static final List<Student> STUDENTS = List.of(
            new Student("26-1029", "Andaya, Samantha M."),
            new Student("26-1102", "Alen, Mariano B."),
            new Student("26-3021", "Bonifacio, Bandres A."),
            new Student("26-1061", "Dasco, Melvis P."),
            new Student("26-1082", "Gizmo, Alan R."),
            new Student("26-7171", "Mais, Corny A."),
            new Student("26-6119", "Nolan, Layla F."),
            new Student("26-9011", "Paris, France R."),
            new Student("26-8762", "Solar, Panel E."),
            new Student("26-9017", "Quezon, Ajerson L."));

    private static final String[] BLOCKS = {"A", "B", "C", "D"};
    private static final int[] BLOCK_SIZES = {10, 6, 9, 7};   // # of Student column in the prototype

    static List<Subject> subjects(Semester semester) {
        return SUBJECTS.stream().filter(s -> s.semester() == semester).toList();
    }

    static List<Section> sections(Subject subject) {
        List<Section> list = new ArrayList<>();
        for (int i = 0; i < BLOCKS.length; i++) {
            list.add(new Section(subject, "BSIT", subject.year(), BLOCKS[i], BLOCK_SIZES[i]));
        }
        return list;
    }

    static List<Student> students(Section section) {
        return STUDENTS.subList(0, Math.min(section.studentCount(), STUDENTS.size()));
    }
}
