package teacher;

enum Semester {
    FIRST("First Semester"), SECOND("Second Semester");
    final String label;
    Semester(String label) { this.label = label; }
}

enum Period {
    MIDTERM("Midterm", "MIDTERM", GradeCalculator.MIDTERM_WEIGHT),
    FINALS("Finals", "FINALS", GradeCalculator.FINALS_WEIGHT);
    final String label, header;
    final double weight;
    Period(String label, String header, double weight) { this.label = label; this.header = header; this.weight = weight; }
}

record Subject(String code, String title, int year, Semester semester) {}

record Section(Subject subject, String program, int year, String block, int studentCount) {
    String code() { return program + "-" + year + block; }
}

record Student(String id, String name) {}
