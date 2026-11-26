package nxp.student;

import java.util.*;
import static nxp.student.StudentData.SEM;

/** Grading rules (GWA scale, pass/fail, averages, overall = both semesters). */
public final class Grading {
    private Grading() {}
    // ---------- grading logic ----------
    public static String gwa(Integer g) {
        if (g == null) return "-.--";
        if (g >= 95) return "1.00"; if (g >= 92) return "1.25"; if (g >= 90) return "1.50"; if (g >= 88) return "1.75";
        if (g >= 85) return "2.00"; if (g >= 82) return "2.25"; if (g >= 79) return "2.50"; if (g >= 76) return "2.75";
        return g >= 75 ? "3.00" : "5.00";
    }
    public static String remark(Integer g) { return g == null ? "PENDING" : g >= 75 ? "PASSED" : "FAILED"; }
    public static double avg(List<Integer> gs) { return gs.stream().filter(Objects::nonNull).mapToInt(i -> i).average().orElse(Double.NaN); }
    public static String f2(double d) { return Double.isNaN(d) ? "--.--" : String.format("%.2f", d); }
    public static double gwaAvg(List<Subj> l) { return l.stream().map(Subj::overall).filter(Objects::nonNull).mapToDouble(g -> Double.parseDouble(gwa(g))).average().orElse(Double.NaN); }
    public static String status(List<Subj> l) {
        long g = l.stream().filter(s -> s.overall() != null).count();
        if (g < l.size()) return "PENDING"; return l.stream().allMatch(s -> s.overall() >= 75) ? "PASSED" : "FAILED";
    }

    // Overall result only exists once BOTH semesters are fully graded; it is the equal-weight mean of the two semesters.
    public static boolean complete(List<Subj> l) { return l.stream().allMatch(s -> s.overall() != null); }
    public static boolean bothDone() { return complete(SEM.get(0)) && complete(SEM.get(1)); }
    public static double overallAvg() { return !bothDone() ? Double.NaN : (avg(SEM.get(0).stream().map(Subj::overall).toList()) + avg(SEM.get(1).stream().map(Subj::overall).toList())) / 2; }
    public static double overallGwa() { return !bothDone() ? Double.NaN : (gwaAvg(SEM.get(0)) + gwaAvg(SEM.get(1))) / 2; }

}
