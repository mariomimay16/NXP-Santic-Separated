package teacher;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;

/**
 * The Grade Management page for ONE student and ONE grading period.
 * Midterm and Finals use this same view; only the period (header text, weight) differs.
 */
final class GradeView extends VBox {

    private static final String[] TITLES = {"Summative Tests", "Recitation/Activity", "Attendance", "Periodical Exam", "Project"};
    private static final String[] BLANK = new String[10];
    static { Arrays.fill(BLANK, ""); }

    private final Section section;
    private final Student student;
    private final Period period;
    private final GradeStore store;
    private final String key;

    private final TextField[] score = new TextField[5];
    private final TextField[] max = new TextField[5];
    private final Label[] scoreErr = new Label[5];
    private final Label[] maxErr = new Label[5];
    private final Label autoClass = Ui.label("", "grade-field-ro");

    private final Label calcBadge = new Label();
    private final Label tClass = Ui.label("--", "tile-value-s");
    private final Label tExam = Ui.label("--", "tile-value-s");
    private final Label tProject = Ui.label("--", "tile-value-s");
    private final Label tTotal = Ui.label("--", "tile-value-l");
    private final Label tWeighted = Ui.label("--", "tile-value-l");
    private final Label finalBadge = new Label();
    private final Label finalDetail = Ui.label("", "card-sub-l");

    private String[] saved;          // last saved snapshot (or blanks)
    private boolean submitted;       // Save was pressed at least once -> show "Required" errors live

    GradeView(Section section, Student student, Period period, GradeStore store) {
        this.section = section;
        this.student = student;
        this.period = period;
        this.store = store;
        this.key = GradeStore.key(section, student, period);
        setSpacing(22);

        String[] loaded = store.load(key);
        saved = loaded != null ? loaded.clone() : BLANK.clone();

        getChildren().addAll(infoCard(), assessmentCard(), summaryCard());

        // populate BEFORE adding listeners so loading never counts as an edit
        for (int i = 0; i < 5; i++) {
            score[i].setText(saved[2 * i]);
            max[i].setText(saved[2 * i + 1]);
        }
        for (int i = 0; i < 5; i++) {
            score[i].textProperty().addListener((o, a, b) -> onEdit());
            max[i].textProperty().addListener((o, a, b) -> onEdit());
        }
        onEdit();
    }

    boolean isDirty() {
        return !Arrays.equals(current(), saved);
    }

    // ------------------------------------------------------------------ cards

    private Node infoCard() {
        GridPane g = new GridPane();
        g.setHgap(40);
        g.setVgap(14);
        g.getStyleClass().add("card");
        g.setStyle("-fx-padding: 28 36 28 36;");
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(30);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(30);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(40);
        g.getColumnConstraints().addAll(c1, c2, c3);

        // Labels are kept exactly as written in the prototype
        g.add(info("Student Name", student.name()), 0, 0);
        g.add(info("Student ID", student.id()), 1, 0);
        g.add(info("Student ID", section.subject().title()), 2, 0);
        g.add(info("Subject Code", section.subject().code()), 0, 1);
        g.add(info("Section", section.code()), 1, 1);
        return g;
    }

    private VBox info(String caption, String value) {
        return new VBox(4, Ui.label(caption, "info-caption"), Ui.label(value, "info-value"));
    }

    private Node assessmentCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 24 24 24 24;");
        card.getChildren().addAll(
                new VBox(2, Ui.label("Assessment Grades", "card-title"),
                        Ui.label("Enter each component\u2019s percentage score (0-100)", "card-sub")),
                gradeTable());

        Label note = Ui.label("All composed weights must total 100% per grading period. "
                + "Scores must be between 0 and 100.", "note");
        note.setWrapText(true);
        note.setMaxWidth(640);
        Button save = new Button("Save and Calculate");
        save.getStyleClass().add("btn-primary");
        Ui.hand(save);
        save.setOnAction(e -> save());
        HBox footer = new HBox(note, Ui.spacer(), save);
        footer.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(footer);
        return card;
    }

    private Node gradeTable() {
        GridPane t = new GridPane();
        t.getStyleClass().add("gtable");
        double[] cols = {36, 22, 21, 21};
        for (double w : cols) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(w);
            t.getColumnConstraints().add(c);
        }

        // header
        put(t, Ui.label("Component", "gh"), 0, 0, 1, 2, Pos.CENTER, "gcell", "gcell-r", "gcell-b");
        put(t, Ui.label(period.header, "gh-cyan"), 1, 0, 3, 1, Pos.CENTER, "gcell", "gcell-b");
        put(t, Ui.label("Weight", "gh"), 1, 1, 1, 1, Pos.CENTER, "gcell", "gcell-b", "gcell-r");
        put(t, Ui.label("Score", "gh"), 2, 1, 1, 1, Pos.CENTER, "gcell", "gcell-b");
        put(t, Ui.label("Max Score", "gh"), 3, 1, 1, 1, Pos.CENTER, "gcell", "gcell-b");

        // rows: 2..4 class standing parts, 5 class standing total (auto), 6 exam, 7 project
        addInputRow(t, 2, 0, true, false);
        addInputRow(t, 3, 1, false, false);
        addInputRow(t, 4, 2, false, false);

        VBox csLeft = compLabel(false, null, "Class Standing", "20% of period grade", null);
        put(t, csLeft, 0, 5, 1, 1, Pos.CENTER_LEFT, "gcell", "gcell-r", "gcell-b", "gcell-row");
        put(t, Ui.label("20%", "gweight"), 1, 5, 1, 1, Pos.CENTER, "gcell", "gcell-r", "gcell-b", "gcell-row");
        autoClass.setPrefSize(126, 38);
        autoClass.setMinSize(126, 38);
        autoClass.setAlignment(Pos.CENTER);
        VBox auto = new VBox(6, autoClass, Ui.label("Automatically Calculated", "auto-caption"));
        auto.setAlignment(Pos.CENTER);
        put(t, auto, 2, 5, 2, 1, Pos.CENTER, "gcell", "gcell-b", "gcell-row");

        addInputRow(t, 6, 3, false, false);
        addInputRow(t, 7, 4, false, true);

        Ui.roundClip(t, 14);
        return t;
    }

    private void addInputRow(GridPane t, int row, int idx, boolean groupHeader, boolean last) {
        boolean sub = idx <= 2;
        String extra = idx == 2 ? "Enter days present / total days" : null;
        VBox left = compLabel(sub, groupHeader ? "Class Standing" : null, TITLES[idx],
                (int) GradeCalculator.WEIGHTS[idx] + "% of period grade", extra);
        String[] base = last ? new String[]{"gcell", "gcell-row"} : new String[]{"gcell", "gcell-b", "gcell-row"};
        String[] baseR = last ? new String[]{"gcell", "gcell-r", "gcell-row"} : new String[]{"gcell", "gcell-r", "gcell-b", "gcell-row"};
        put(t, left, 0, row, 1, 1, Pos.CENTER_LEFT, baseR);
        put(t, Ui.label((int) GradeCalculator.WEIGHTS[idx] + "%", "gweight"), 1, row, 1, 1, Pos.CENTER, baseR);

        boolean att = idx == GradeCalculator.ATTENDANCE;
        score[idx] = field(att ? "Days" : "0-100");
        max[idx] = field(att ? "Total Days" : "0-100");
        scoreErr[idx] = errLabel();
        maxErr[idx] = errLabel();
        put(t, new VBox(2, score[idx], scoreErr[idx]), 2, row, 1, 1, Pos.CENTER, base);
        put(t, new VBox(2, max[idx], maxErr[idx]), 3, row, 1, 1, Pos.CENTER, base);
        ((VBox) score[idx].getParent()).setAlignment(Pos.CENTER);
        ((VBox) max[idx].getParent()).setAlignment(Pos.CENTER);
    }

    private VBox compLabel(boolean sub, String groupHeader, String title, String caption, String extra) {
        VBox v = new VBox(1);
        v.setAlignment(Pos.CENTER_LEFT);
        if (groupHeader != null) v.getChildren().add(Ui.label(groupHeader, "comp-main"));
        Label t = Ui.label(title, sub ? "comp-sub" : "comp-main");
        Label c = Ui.label(caption, "comp-caption");
        if (sub) { t.setStyle("-fx-padding: 0 0 0 30;"); c.setStyle("-fx-padding: 0 0 0 33;"); }
        else { c.setStyle("-fx-padding: 0 0 0 34;"); }
        v.getChildren().addAll(t, c);
        if (extra != null) {
            Label x = Ui.label(extra, "comp-extra");
            x.setStyle("-fx-padding: 0 0 0 33;");
            v.getChildren().add(x);
        }
        return v;
    }

    private Node summaryCard() {
        VBox card = new VBox(18);
        card.getStyleClass().add("card");
        card.setStyle("-fx-padding: 24 24 24 24;");

        calcBadge.getStyleClass().add("badge");
        HBox titleRow = new HBox(new VBox(2, Ui.label("Grade Summary", "card-title"),
                Ui.label("Automatically calculated results.", "card-sub")), Ui.spacer(), calcBadge);
        titleRow.setAlignment(Pos.TOP_LEFT);

        GridPane small = threeColumns();
        small.add(tile("Total Percentage of Class Standing", tClass, true), 0, 0);
        small.add(tile("Total Percentage of Periodical Exam", tExam, true), 1, 0);
        small.add(tile("Total Percentage of Project", tProject, true), 2, 0);

        GridPane big = threeColumns();
        big.add(tile("Total Percentage", tTotal, false), 0, 0);
        big.add(tile(period.label + " Weight", Ui.label((int) period.weight + "%", "tile-value-l"), false), 1, 0);
        big.add(tile(period.label + " Weighted Grade", tWeighted, false), 2, 0);

        Region divider = new Region();
        divider.getStyleClass().add("divider");
        divider.setPrefHeight(1);

        finalBadge.getStyleClass().add("badge");
        finalDetail.setWrapText(true);
        VBox finalText = new VBox(6, Ui.label("Final Result", "card-title"),
                Ui.label(String.format("Passing Grade: %.2f", GradeCalculator.PASSING_GRADE), "card-sub-l"),
                finalDetail);
        finalText.setAlignment(Pos.TOP_LEFT);
        HBox finalRow = new HBox(24, finalText, Ui.spacer(), finalBadge);
        finalRow.setAlignment(Pos.TOP_LEFT);

        card.getChildren().addAll(titleRow, small, big, divider, finalRow);
        return card;
    }

    private GridPane threeColumns() {
        GridPane g = new GridPane();
        g.setHgap(28);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints c = new ColumnConstraints();
            c.setPercentWidth(33.333);
            g.getColumnConstraints().add(c);
        }
        return g;
    }

    private VBox tile(String caption, Label value, boolean small) {
        Label c = Ui.label(caption, small ? "tile-caption-s" : "tile-caption-l");
        c.setWrapText(true);
        VBox v = new VBox(small ? 6 : 8, c, value);
        v.getStyleClass().addAll("tile", small ? "tile-s" : "tile-l");
        v.setMaxWidth(Double.MAX_VALUE);
        v.setAlignment(Pos.TOP_LEFT);
        return v;
    }

    // ------------------------------------------------------------------ inputs

    private TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("grade-field");
        f.setPrefSize(126, 38);
        f.setMinSize(126, 38);
        f.setMaxSize(126, 38);
        f.setAlignment(Pos.CENTER);
        // digits with one optional decimal point only
        f.setTextFormatter(new TextFormatter<String>(c ->
                c.getControlNewText().matches("\\d{0,3}(\\.\\d{0,2})?") ? c : null));
        return f;
    }

    private Label errLabel() {
        Label l = Ui.label("", "field-error");
        l.setManaged(false);
        l.setVisible(false);
        return l;
    }

    private String[] current() {
        String[] c = new String[10];
        for (int i = 0; i < 5; i++) {
            c[2 * i] = score[i].getText().trim();
            c[2 * i + 1] = max[i].getText().trim();
        }
        return c;
    }

    private void onEdit() {
        String[] cur = current();
        Double cs = GradeCalculator.classStanding(cur);
        autoClass.setText(cs == null ? "" : String.format("%.2f%%", cs));
        if (submitted) showErrors();
        refreshSummary();
    }

    private void showErrors() {
        for (int i = 0; i < 5; i++) {
            apply(score[i], scoreErr[i], GradeCalculator.checkScore(score[i].getText(), max[i].getText()));
            apply(max[i], maxErr[i], GradeCalculator.checkMax(max[i].getText()));
        }
    }

    private void apply(TextField f, Label err, String message) {
        boolean bad = message != null;
        f.getStyleClass().remove("grade-field-error");
        if (bad) f.getStyleClass().add("grade-field-error");
        err.setText(bad ? message : "");
        err.setManaged(bad);
        err.setVisible(bad);
    }

    private void save() {
        submitted = true;
        showErrors();
        String[] cur = current();
        if (!GradeCalculator.isValid(cur)) return;       // red "Required"/"Invalid" markers stay visible
        store.save(key, cur);
        saved = cur.clone();
        refreshSummary();
    }

    // ------------------------------------------------------------------ summary

    private void refreshSummary() {
        boolean calculated = !isDirty() && store.load(key) != null;
        GradeCalculator.PeriodResult r = calculated ? GradeCalculator.compute(saved, period.weight) : null;

        setBadge(calcBadge, r != null ? "Calculated" : "Not Calculated", r != null ? "badge-blue" : "badge-gray");
        tClass.setText(r == null ? "--" : pct(r.classStanding()));
        tExam.setText(r == null ? "--" : pct(r.periodicalExam()));
        tProject.setText(r == null ? "--" : pct(r.project()));
        tTotal.setText(r == null ? "--" : pct(r.totalPercentage()));
        tWeighted.setText(r == null ? "--" : String.format("%.2f", r.weightedGrade()));

        // Final result needs BOTH saved periods
        GradeCalculator.PeriodResult mid = stored(Period.MIDTERM);
        GradeCalculator.PeriodResult fin = stored(Period.FINALS);
        if (mid != null && fin != null) {
            GradeCalculator.FinalResult f = GradeCalculator.finalResult(mid, fin);
            finalDetail.setText(String.format("Midterm %.2f + Finals %.2f = Final Grade %.2f   \u2022   GWA %.2f",
                    f.midtermWeighted(), f.finalsWeighted(), f.finalGrade(), f.gwa()));
            setBadge(finalBadge, f.passed() ? "PASSED" : "FAILED", f.passed() ? "badge-pass" : "badge-fail");
        } else {
            String waiting = mid == null && fin == null ? "Midterm and Finals" : (mid == null ? "Midterm" : "Finals");
            finalDetail.setText("Waiting for " + waiting + " grades to be saved");
            setBadge(finalBadge, "Awaiting Grades", "badge-gray");
        }
    }

    private GradeCalculator.PeriodResult stored(Period p) {
        String[] raw = store.load(GradeStore.key(section, student, p));
        return raw == null ? null : GradeCalculator.compute(raw, p.weight);
    }

    private static String pct(double v) { return String.format("%.2f%%", v); }

    private static void setBadge(Label l, String text, String style) {
        l.setText(text);
        l.getStyleClass().removeAll("badge-gray", "badge-blue", "badge-pass", "badge-fail");
        l.getStyleClass().add(style);
    }

    // ------------------------------------------------------------------ grid helper

    private static void put(GridPane g, Node n, int col, int row, int colSpan, int rowSpan, Pos align, String... styles) {
        StackPane cell = new StackPane(n);
        cell.getStyleClass().addAll(styles);
        StackPane.setAlignment(n, align);
        cell.setMinHeight(rowSpan == 2 ? 120 : (row == 0 || row == 1 ? 56 : 86));
        GridPane.setHgrow(cell, Priority.ALWAYS);
        GridPane.setHalignment(cell, HPos.LEFT);
        GridPane.setValignment(cell, VPos.TOP);
        GridPane.setFillWidth(cell, true);
        GridPane.setFillHeight(cell, true);
        g.add(cell, col, row, colSpan, rowSpan);
    }
}
