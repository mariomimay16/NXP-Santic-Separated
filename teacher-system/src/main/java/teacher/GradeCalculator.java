package teacher;

/**
 * Pure grade math (no JavaFX) so it can be unit-tested.
 *
 * Per grading period (Midterm or Finals), every component is
 *   (score / maxScore) * componentWeight
 *
 *   Class Standing (20%) = Summative (10) + Recitation/Activity (5) + Attendance (5)
 *   Periodical Exam (30%)
 *   Project (50%)
 *   Total Percentage     = Class Standing + Periodical Exam + Project   (out of 100)
 *
 *   Midterm Weighted Grade = Total Percentage * 40%
 *   Finals  Weighted Grade = Total Percentage * 60%
 *   Final Grade            = Midterm Weighted + Finals Weighted
 *
 * For ATTENDANCE the "score" is days present and the "max score" is total days.
 */
public final class GradeCalculator {

    private GradeCalculator() {}

    // Component order used everywhere: index 0..4
    public static final int SUMMATIVE = 0, RECITATION = 1, ATTENDANCE = 2, PERIODICAL = 3, PROJECT = 4;
    public static final double[] WEIGHTS = {10, 5, 5, 30, 50};
    public static final double CLASS_STANDING_WEIGHT = 20;     // 10 + 5 + 5
    public static final double MIDTERM_WEIGHT = 40;
    public static final double FINALS_WEIGHT = 60;
    public static final double PASSING_GRADE = 75;

    public record PeriodResult(double summative, double recitation, double attendance,
                               double classStanding, double periodicalExam, double project,
                               double totalPercentage, double periodWeight, double weightedGrade) {}

    public record FinalResult(double midtermWeighted, double finalsWeighted,
                              double finalGrade, double gwa, boolean passed) {}

    // ---------- validation ----------

    /** Error for the Score field, or null if valid. {@code max} may be blank/invalid. */
    public static String checkScore(String score, String max) {
        Double s = parse(score);
        if (isBlank(score)) return "Required";
        if (s == null) return "Invalid";
        if (s < 0 || s > 100) return "0-100";
        Double m = parse(max);
        if (m != null && m > 0 && s > m) return "Over max";
        return null;
    }

    /** Error for the Max Score field, or null if valid. */
    public static String checkMax(String max) {
        Double m = parse(max);
        if (isBlank(max)) return "Required";
        if (m == null) return "Invalid";
        if (m <= 0 || m > 100) return "1-100";
        return null;
    }

    public static boolean isValid(String[] raw) {
        if (raw == null || raw.length != 10) return false;
        for (int i = 0; i < 5; i++) {
            if (checkScore(raw[2 * i], raw[2 * i + 1]) != null) return false;
            if (checkMax(raw[2 * i + 1]) != null) return false;
        }
        return true;
    }

    // ---------- computation ----------

    /** raw = {score0, max0, score1, max1, ...} in component order. Null if invalid. */
    public static PeriodResult compute(String[] raw, double periodWeight) {
        if (!isValid(raw)) return null;
        double[] part = new double[5];
        for (int i = 0; i < 5; i++) {
            part[i] = parse(raw[2 * i]) / parse(raw[2 * i + 1]) * WEIGHTS[i];
        }
        double classStanding = part[SUMMATIVE] + part[RECITATION] + part[ATTENDANCE];
        double total = classStanding + part[PERIODICAL] + part[PROJECT];
        double weighted = total * periodWeight / 100.0;
        return new PeriodResult(part[SUMMATIVE], part[RECITATION], part[ATTENDANCE],
                classStanding, part[PERIODICAL], part[PROJECT], total, periodWeight, weighted);
    }

    /** Class standing only (live preview); null until all three class-standing rows are valid. */
    public static Double classStanding(String[] raw) {
        double sum = 0;
        for (int i = 0; i < 3; i++) {
            if (checkScore(raw[2 * i], raw[2 * i + 1]) != null || checkMax(raw[2 * i + 1]) != null) return null;
            sum += parse(raw[2 * i]) / parse(raw[2 * i + 1]) * WEIGHTS[i];
        }
        return sum;
    }

    public static FinalResult finalResult(PeriodResult midterm, PeriodResult finals) {
        double fg = midterm.weightedGrade() + finals.weightedGrade();
        return new FinalResult(midterm.weightedGrade(), finals.weightedGrade(), fg, gwa(fg), fg >= PASSING_GRADE);
    }

    /**
     * Final grade -> GWA (1.00 best, 3.00 lowest pass, 5.00 fail).
     * Calibrated so 93 -> 1.25 as in the sample (IT-100: 36.00 + 57.00 = 93 -> GWA 1.25).
     * Change the bands here if your school uses a different table.
     */
    public static double gwa(double finalGrade) {
        double g = Math.round(finalGrade * 100.0) / 100.0;
        if (g >= 96) return 1.00;
        if (g >= 93) return 1.25;
        if (g >= 90) return 1.50;
        if (g >= 87) return 1.75;
        if (g >= 84) return 2.00;
        if (g >= 81) return 2.25;
        if (g >= 78) return 2.50;
        if (g >= 76) return 2.75;
        if (g >= 75) return 3.00;
        return 5.00;
    }

    // ---------- helpers ----------

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    public static Double parse(String s) {
        if (isBlank(s)) return null;
        try {
            double d = Double.parseDouble(s.trim());
            return Double.isNaN(d) || Double.isInfinite(d) ? null : d;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
