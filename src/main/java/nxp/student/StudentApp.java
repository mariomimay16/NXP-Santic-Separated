package nxp.student;

import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import java.util.*;
import java.util.function.Function;
import nxp.core.*;
import static nxp.core.Ui.*;
import static nxp.student.Grading.*;
import static nxp.student.StudentData.*;

/** STUDENT portal: dashboard, grade tables, profile. Teacher/Admin portals should be separate classes like this one. */
public class StudentApp implements Screen {
    static final double DW = 1600, DX = DW - 1280, F = (DW - 256) / 1024.0;   // design width (smaller UI), right-edge shift, summary spread
    private final Router router; private final User user; private final StudentProfile P;
    public StudentApp(Router router, User user) { this.router = router; this.user = user; this.P = StudentData.profileFor(user); }
    @Override public double designWidth() { return DW; }

    String page = "dash", period = "Midterm", search = "", filter = ""; int sem = 0;
    boolean gradesOpen; int subOpen = -1; String popup = "";
    TextField searchField; double scroll;
    double dh = 800; final List<Node> late = new ArrayList<>();

    // ================= APP SHELL =================
    @Override public Pane build(double dh) {
        this.dh = dh;
        Pane p = new Pane(); p.setStyle("-fx-background-color:linear-gradient(to bottom,#021a33,#00223b);"); p.getChildren().addAll(content(), sidebar());
        if (popup.equals("profile")) p.getChildren().add(profileModal()); return p;
    }
    void refresh() { router.render(); }
    Node sidebar() {
        Pane s = new Pane(); Rectangle bg = new Rectangle(256, dh); bg.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, new Stop(0, Color.web("#031729")), new Stop(1, Color.web("#021120")))); s.getChildren().add(bg);
        ImageView av = at("avatar", 14, 54); av.setFitWidth(44); av.setFitHeight(44); s.getChildren().addAll(av, text("Hello, " + P.firstName(), 69, 77, 26, FontWeight.BOLD, Color.WHITE), rr(69, 85, 127, 17, 8.5, Color.web("#00447f"), null), ctext("Student Dashboard", 132, 98, 11, FontWeight.BOLD, Color.web("#00d0ff")));
        boolean dash = page.equals("dash"), gr = page.equals("grades");
        s.getChildren().addAll(dash ? at("nav_dash_on", 25, 154) : at("nav_dash_off", 41, 165), hit(25, 154, 203, 41, () -> { page = "dash"; popup = ""; refresh(); }));
        // Grades: pill 25..228 (same slot), arrow at x=204
        s.getChildren().addAll(gr ? at("nav_grades_on", 25, 214) : at("nav_grades_off", 41, 224), arrow(204, 235, gradesOpen, 6, Color.web("#dbe7f3")), hit(25, 214, 203, 41, () -> { gradesOpen = !gradesOpen; if (gradesOpen) { page = "grades"; subOpen = sem == 2 ? 0 : sem; } popup = ""; refresh(); }));
        if (gradesOpen) {
            double y = 286;
            for (int k = 0; k < 2; k++) {
                boolean open = subOpen == k; final int kk = k;
                s.getChildren().addAll(ctext(SEM_NAMES[k], 150, y + 5, 14, FontWeight.BOLD, Color.web("#dbe7f3")), arrow(235, y, open, 6, Color.web("#dbe7f3")), hit(55, y - 14, 200, 28, () -> { subOpen = open ? -1 : kk; refresh(); }));
                y += 38;
                if (open) for (String per : new String[]{"Midterm", "Finals"}) {
                    boolean on = gr && sem == k && period.equals(per);
                    if (on) s.getChildren().add(rr(107, y - 14, 95, 27, 5, Color.web("#0066cc"), null));
                    s.getChildren().addAll(ctext(per, 155, y + 5, 13, FontWeight.BOLD, on ? Color.WHITE : Color.web("#dbe7f3")), hit(100, y - 14, 110, 28, () -> { sem = kk; period = per; page = "grades"; search = ""; filter = ""; popup = ""; refresh(); }));
                    y += 38;
                }
            }
        }
        s.getChildren().addAll(at("ic_logout", 42, dh - 70), text("Logout", 80, dh - 51, 14, FontWeight.BOLD, Color.web("#c3d3e2")), hit(36, dh - 75, 120, 36, () -> { page = "dash"; sem = 0; gradesOpen = false; popup = ""; search = filter = ""; router.logout(); }));
        return s;
    }
    ScrollPane content() {
        Pane c = new Pane(); c.setStyle("-fx-background-color:transparent;");
        double h = page.equals("dash") ? dashboard(c) : grades(c);
        c.setPrefSize(DW - 256, Math.max(dh, h + 40)); ScrollPane sp = new ScrollPane(c); sp.setLayoutX(256); sp.setPrefSize(DW - 256, dh); sp.setMaxSize(DW - 256, dh);
        sp.setFitToWidth(true); sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER); sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setStyle("-fx-background:transparent;-fx-background-color:transparent;"); sp.setVvalue(scroll); sp.vvalueProperty().addListener((o, a, b) -> scroll = b.doubleValue()); return sp;
    }
    // x coordinates below are page x minus 256
    void header(Pane c, String title, double ty, String sub, boolean picker) {
        c.getChildren().add(text(title, 67, ty + 13, 40, FontWeight.BOLD, Color.WHITE));
        Text t = text(sub, 69, ty + 56, 17, FontWeight.BOLD, DIM); c.getChildren().add(t);
        if (picker) { double ax = 69 + t.getLayoutBounds().getWidth() + 22; c.getChildren().addAll(arrow(ax, ty + 50, popup.equals("sem") ? false : true, 8, Color.web("#c9d6e2")), hit(69, ty + 36, t.getLayoutBounds().getWidth() + 50, 30, () -> { popup = popup.equals("sem") ? "" : "sem"; refresh(); }));
            if (popup.equals("sem")) { String[] it = {"First Semester", "Second Semester", "Overall Average"};
                for (int i = 0; i < 3; i++) { final int k = i; Pane row = new Pane(); row.setLayoutX(ax - 30); row.setLayoutY(ty + 70 + i * 32); row.setPrefSize(183, 32); row.setStyle("-fx-background-color:#031d36;-fx-border-color:#41688a;-fx-border-width:1;");
                    row.getChildren().add(text(it[i], 9, 21, 13, FontWeight.BOLD, Color.web("#c9d6e2"))); Button b = hit(0, 0, 183, 32, () -> { sem = k; search = filter = ""; popup = ""; scroll = 0; refresh(); }); row.getChildren().add(b); late.add(row); } } }
        Pane card = new Pane(); card.setLayoutX(714 + DX); card.setLayoutY(ty - 34); card.setPrefSize(285, 91);
        ImageView pav = at("avatar", 21, 12); pav.setFitWidth(68); pav.setFitHeight(68);
        card.getChildren().addAll(rr(0, 0, 285, 91, 8, Color.web("#00223b"), Color.web("#143253")), pav, text(P.fullName(), 100, 31, 20, FontWeight.BOLD, Color.WHITE), text("Student ID: " + P.studentId(), 100, 52, 12, FontWeight.BOLD, Color.web("#a7bacb")), text(P.label(), 100, 72, 12, FontWeight.BOLD, Color.web("#00d0ff")));
        card.setOnMouseClicked(e -> { popup = "profile"; refresh(); }); card.setCursor(Cursor.HAND); c.getChildren().add(card);
    }
    void statCard(Pane c, double x, double y, double w, double h, String icon, String l1, String l2, Node big, String f1, String f2) {
        c.getChildren().addAll(rr(x, y, w, h, 12, Color.web("#00223b"), Color.web("#143253")), at(icon, x + 26, y + 12));
        c.getChildren().add(text(l1, x + 93, y + 31, 17, FontWeight.BOLD, TXT)); if (!l2.isEmpty()) c.getChildren().add(text(l2, x + 93, y + 53, 17, FontWeight.BOLD, TXT));
        c.getChildren().add(big); c.getChildren().add(text(f1, x + 27, y + 142, 13, FontWeight.BOLD, DIM)); if (!f2.isEmpty()) c.getChildren().add(text(f2, x + 27, y + 160, 13, FontWeight.BOLD, DIM));
    }
    double dashboard(Pane c) {
        List<Subj> all = new ArrayList<>(); SEM.forEach(all::addAll);
        List<Subj> l = sem == 2 ? all : SEM.get(sem); String nm = sem == 2 ? "Overall Average" : SEM_NAMES[sem];
        header(c, "Student Grades Overview", 85, "School Year 2026-2027 \u2022 Read-Only Access \u2022 " + nm, true);
        long gr = l.stream().filter(s -> s.overall() != null).count(); String st = status(l); double a = sem == 2 ? overallAvg() : avg(l.stream().map(Subj::overall).toList());
        boolean pend = st.equals("PENDING");
        Text big1 = text(f2(a), 96, 300, 45, FontWeight.BOLD, Color.WHITE); big1.setX(96);
        if (sem == 2) statCard(c, 69, 183, 390, 178, "ic_medal", "Overalll Average", "", big1, "Auto-computed across " + l.size(), "subjects");
        else statCard(c, 69, 183, 390, 178, "ic_medal", "General Average for", nm, big1, "Auto-computed across " + l.size(), "subjects");
        statCard(c, 499, 180, 390, 178, st.equals("PASSED") ? "ic_shield" : st.equals("FAILED") ? "ic_x" : "ic_clock", "Overall Academic", "Status", new Group(), st.equals("FAILED") ? "One or more subjects below" : "All subjects meet passing", st.equals("FAILED") ? "passing criteria" : "criteria");
        c.getChildren().add(at("big_" + (pend ? "pend" : st.equals("PASSED") ? "pass" : "fail"), 524, 257));
        statCard(c, 929, 186, 390, 178, "ic_book", "Enrolled Subjects", "", text(l.size() + " Subjects", 955, 300, 42, FontWeight.BOLD, Color.WHITE), gr + " Graded   \u2022 " + (l.size() - gr) + " Pending", "");
        double r = sem == 2 ? overallCard(c, all, a) : tableCard(c, "Subject Grade Breakdown", l, true, 400, a);
        c.getChildren().addAll(late); late.clear(); return r;
    }
    double grades(Pane c) {
        int k = sem == 2 ? 0 : sem; sem = k;
        header(c, "Student Grades", 85, "School Year 2026-2027 \u2022 Read-Only Access \u2022 " + SEM_NAMES[k], false);
        double r = tableCard(c, period, SEM.get(k), false, 163, 0);
        c.getChildren().addAll(late); late.clear(); return r;
    }

    // ---------- tables ----------
    double tableCard(Pane c, String title, List<Subj> src, boolean overall, double top, double a) {
        Function<Subj, Integer> gv = s -> overall ? s.overall() : period.equals("Midterm") ? s.mid() : s.fin();
        List<Subj> rows = new ArrayList<>(src); rows.removeIf(s -> !(s.code() + " " + s.title()).toLowerCase().contains(search.toLowerCase()));
        switch (filter) {
            case "Passed" -> rows.removeIf(s -> !"PASSED".equals(remark(gv.apply(s))));
            case "Failed" -> rows.removeIf(s -> !"FAILED".equals(remark(gv.apply(s))));
            case "Pending" -> rows.removeIf(s -> gv.apply(s) != null);
            case "Highest to Lowest" -> rows.sort(Comparator.comparing((Subj s) -> gv.apply(s) == null ? -1 : gv.apply(s)).reversed());
            case "Lowest to Highest" -> rows.sort(Comparator.comparing((Subj s) -> gv.apply(s) == null ? 999 : gv.apply(s)));
            default -> {}
        }
        double rowsH = Math.max(1, rows.size()) * 61, h = 123 + rowsH + (overall ? 90 : 0);
        c.getChildren().add(rr(67, top, 932 + DX, h, 8, Color.web("#00223b"), Color.web("#143253")));
        c.getChildren().add(text(title, 96, top + 33, 24, FontWeight.BOLD, Color.WHITE));
        searchField = new TextField(search); searchField.setPromptText("Search Subject"); searchField.setLayoutX(560 + DX); searchField.setLayoutY(top + 13); searchField.setPrefSize(372, 30);
        searchField.setStyle("-fx-background-color:#00203a;-fx-background-radius:15;-fx-border-color:#2f5577;-fx-border-radius:15;-fx-text-fill:white;-fx-prompt-text-fill:#9db3c4;-fx-font-size:13px;-fx-font-weight:bold;-fx-padding:0 0 0 40;-fx-font-family:'Inter';");
        searchField.setOnKeyReleased(e -> { search = searchField.getText(); refresh(); Platform.runLater(() -> { searchField.requestFocus(); searchField.end(); }); });
        c.getChildren().addAll(searchField, at("ic_search", 574 + DX, top + 20), at("ic_filter", 948 + DX, top + 19), hit(940 + DX, top + 12, 40, 32, () -> { popup = popup.equals("filter") ? "" : "filter"; refresh(); }));
        c.getChildren().add(new Line(67, top + 63, 999 + DX, top + 63) {{ setStroke(Color.web("#204065")); }});
        Rectangle hb = new Rectangle(68, top + 64, 930 + DX, 60); hb.setFill(Color.web("#031f3a")); c.getChildren().add(hb);
        double hy = top + 98; Color hc = Color.web("#9db3c4");
        c.getChildren().addAll(text("#", 96, hy - 2, 19, FontWeight.BOLD, hc), text("Subject Code", 169, hy, 14, FontWeight.BOLD, hc), text("Subject Title", 300, hy, 14, FontWeight.BOLD, hc));
        if (overall) c.getChildren().addAll(text("Overall Final Grade", 649 + DX, hy, 14, FontWeight.BOLD, hc), text("GWA", 805 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        else c.getChildren().addAll(text("Weighted Grade", 747 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        double y = top + 124; int i = 1;
        for (Subj s : rows) {
            Integer v = gv.apply(s); double cy = y + 36;
            c.getChildren().add(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }});
            c.getChildren().addAll(text("" + i, 96, cy, 17, FontWeight.BOLD, TXT), text(s.code(), 169, cy, 15, FontWeight.BOLD, TXT), text(s.title(), 300, cy, 15, FontWeight.BOLD, TXT));
            Color cy2 = Color.web("#00d0ff"); String vs = v == null ? "--" : "" + v;
            if (overall) c.getChildren().addAll(ctext(vs, 659 + DX, cy + 1, 15, FontWeight.BOLD, cy2), text(gwa(v), 807 + DX, cy, 15, FontWeight.BOLD, TXT));
            else c.getChildren().add(ctext(vs, 802 + DX, cy + 1, 15, FontWeight.BOLD, cy2));
            String r = remark(v); c.getChildren().add(at("b_" + (r.equals("PASSED") ? "pass" : r.equals("FAILED") ? "fail" : "pend"), 899 + DX, cy - 15));
            y += 61; i++;
        }
        if (rows.isEmpty()) c.getChildren().add(ctext("No subjects found", 533, y + 36, 15, FontWeight.BOLD, TXT));
        if (overall) {
            long g = src.stream().filter(s -> s.overall() != null).count(); y = top + 124 + Math.max(1, rows.size()) * 61;
            c.getChildren().add(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }});
            c.getChildren().add(text("SUMMARY", 96, y + 52, 14, FontWeight.BOLD, hc));
            sumCell(c, 354 * F, y, "General Average", f2(a)); sumCell(c, 587 * F, y, "Average Grade Point", Double.isNaN(gwaAvg(src)) ? "-.-" : f2(gwaAvg(src)));
            sumCell(c, 834 * F, y, "Subjects Count", g + " / " + src.size()); if (g == 0) c.getChildren().add(ctext("(Graded)", 834 * F, y + 82, 11, FontWeight.BOLD, hc));
        }
        if (popup.equals("filter")) filterMenu(c, top);
        return top + h;
    }
    void sumCell(Pane c, double cx, double y, String label, String v) { c.getChildren().addAll(ctext(label, cx, y + 24, 14, FontWeight.BOLD, Color.web("#9db3c4")), ctext(v, cx, y + 60, 34, FontWeight.BOLD, Color.WHITE)); }
    void filterMenu(Pane c, double top) {
        String[] it = {"Passed", "Failed", "Pending", "Highest to Lowest", "Lowest to Highest"};
        for (int i = 0; i < it.length; i++) { final String n = it[i]; Pane r = new Pane(); r.setLayoutX(824 + DX); r.setLayoutY(top + 46 + i * 33); r.setPrefSize(176, 33);
            r.setStyle("-fx-background-color:" + (filter.equals(n) ? "#0b3f73" : "#031d36") + ";-fx-border-color:#41688a;-fx-border-width:1;");
            r.getChildren().addAll(text(n, 8, 21, 12, FontWeight.BOLD, Color.web("#c9d6e2")), hit(0, 0, 176, 33, () -> { filter = filter.equals(n) ? "" : n; popup = ""; refresh(); })); c.getChildren().add(r); }
    }
    double overallCard(Pane c, List<Subj> all, double a) {
        double top = 400, h = 123 + 2 * 61 + 90;
        c.getChildren().add(rr(67, top, 932 + DX, h, 8, Color.web("#00223b"), Color.web("#143253")));
        c.getChildren().addAll(text("Overall Average", 96, top + 33, 24, FontWeight.BOLD, Color.WHITE), at("ic_filter", 948 + DX, top + 19), new Line(67, top + 63, 999 + DX, top + 63) {{ setStroke(Color.web("#204065")); }});
        Rectangle hb = new Rectangle(68, top + 64, 930 + DX, 60); hb.setFill(Color.web("#031f3a")); c.getChildren().add(hb);
        double hy = top + 98; Color hc = Color.web("#9db3c4");
        c.getChildren().addAll(text("#", 96, hy - 2, 19, FontWeight.BOLD, hc), text("Grading Period", 169, hy, 14, FontWeight.BOLD, hc), text("# of Subjects", 330, hy, 14, FontWeight.BOLD, hc),
            text("Overall Final Grade", 649 + DX, hy, 14, FontWeight.BOLD, hc), text("GWA", 805 + DX, hy, 14, FontWeight.BOLD, hc), text("Remarks", 899 + DX, hy, 14, FontWeight.BOLD, hc));
        double y = top + 124;
        for (int i = 0; i < 2; i++) { List<Subj> l = SEM.get(i); double cy = y + 36; double av = avg(l.stream().map(Subj::overall).toList()); String s = status(l);
            c.getChildren().addAll(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }}, text("" + (i + 1), 96, cy, 17, FontWeight.BOLD, TXT), text(SEM_NAMES[i], 169, cy, 15, FontWeight.BOLD, TXT), text("" + l.size(), 345, cy, 15, FontWeight.BOLD, TXT),
                ctext(Double.isNaN(av) ? "--" : f2(av), 659 + DX, cy + 1, 15, FontWeight.BOLD, Color.web("#00d0ff")), text(Double.isNaN(gwaAvg(l)) ? "-.--" : f2(gwaAvg(l)), 807 + DX, cy, 15, FontWeight.BOLD, TXT),
                at("b_" + (s.equals("PASSED") ? "pass" : s.equals("FAILED") ? "fail" : "pend"), 899 + DX, cy - 15)); y += 61; }
        c.getChildren().addAll(new Line(67, y, 999 + DX, y) {{ setStroke(LINE); }}, text("SUMMARY", 96, y + 52, 14, FontWeight.BOLD, hc));
        sumCell(c, 440 * F, y, "Overalll Average", f2(a)); sumCell(c, 700 * F, y, "GWA", Double.isNaN(overallGwa()) ? "-.--" : f2(overallGwa()));
        return top + h;
    }

    // ---------- profile modal ----------
    Node profileModal() {
        Pane m = new Pane(); m.setPrefSize(DW, dh); m.setStyle("-fx-background-color:rgba(0,0,0,.35);"); m.setOnMouseClicked(e -> {});
        m.getChildren().add(rr(522, 42, 555, 716, 36, Color.web("#0a2d52", .96), Color.web("#4a78a6", .7)));
        ImageView ph = at("avatar", 722, 118); ph.setFitWidth(130); ph.setFitHeight(130);
        m.getChildren().addAll(ctext("Student Profile", 800, 99, 40, FontWeight.BOLD, Color.WHITE), at("ic_close", 1012, 68), hit(1000, 58, 50, 44, () -> { popup = ""; refresh(); }),
            ph, at("ic_plus", 828, 215), ctext("Profile Photo", 788, 280, 15, FontWeight.BOLD, Color.web("#c3d3e2")));
        String[][] f = {{"Student's Name:", P.fullName()}, {"Student's ID number", P.studentId()}, {"Student's Email", P.email()},
            {"Program", P.program()}, {"Year & Block", P.yearBlock()}};
        for (int i = 0; i < f.length; i++) { double y = 316 + i * 87;
            m.getChildren().addAll(text(f[i][0], 543, y, 17, FontWeight.BOLD, Color.web("#00d0ff")), rr(558, y + 12, 490, 42, 6, Color.web("#031e38"), Color.web("#2f5577")), text(f[i][1], 580, y + 40, 18, FontWeight.BOLD, Color.WHITE)); }
        for (Node n : m.getChildren()) n.setTranslateX((DW - 1280) / 2);   // centre in the wider layout
        return m;
    }
}
