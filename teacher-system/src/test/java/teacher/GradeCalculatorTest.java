package teacher;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class GradeCalculatorTest {

    // Summative 90/100, Recitation 4/5, Attendance 5/5 days, Exam 90/100, Project 90/100
    private static final String[] MID = {"90", "100", "4", "5", "5", "5", "90", "100", "90", "100"};
    // Summative 95/100, Recitation 90/100, Attendance 5/5 days, Exam 95/100, Project 95/100
    private static final String[] FIN = {"95", "100", "90", "100", "5", "5", "95", "100", "95", "100"};

    @Test
    void midtermParts() {
        var r = GradeCalculator.compute(MID, GradeCalculator.MIDTERM_WEIGHT);
        assertEquals(18, r.classStanding(), 1e-9);
        assertEquals(27, r.periodicalExam(), 1e-9);
        assertEquals(45, r.project(), 1e-9);
        assertEquals(90, r.totalPercentage(), 1e-9);
        assertEquals(36, r.weightedGrade(), 1e-9);
    }

    @Test
    void sampleFromSpec_36_plus_57_is_93_gwa_125_passed() {
        var mid = GradeCalculator.compute(MID, GradeCalculator.MIDTERM_WEIGHT);
        var fin = GradeCalculator.compute(FIN, GradeCalculator.FINALS_WEIGHT);
        assertEquals(57, fin.weightedGrade(), 1e-9);
        var f = GradeCalculator.finalResult(mid, fin);
        assertEquals(93, f.finalGrade(), 1e-9);
        assertEquals(1.25, f.gwa(), 1e-9);
        assertTrue(f.passed());
    }

    @Test
    void failingGrade() {
        String[] low = {"10", "100", "1", "5", "1", "5", "50", "100", "50", "100"};
        var f = GradeCalculator.finalResult(GradeCalculator.compute(low, 40), GradeCalculator.compute(low, 60));
        assertFalse(f.passed());
        assertEquals(5.0, f.gwa(), 1e-9);
    }

    @Test
    void validation() {
        assertEquals("Required", GradeCalculator.checkScore("", "30"));
        assertEquals("Over max", GradeCalculator.checkScore("31", "30"));
        assertNull(GradeCalculator.checkScore("25", "30"));
        assertEquals("1-100", GradeCalculator.checkMax("0"));
        assertNull(GradeCalculator.compute(new String[10], 40));
    }
}
