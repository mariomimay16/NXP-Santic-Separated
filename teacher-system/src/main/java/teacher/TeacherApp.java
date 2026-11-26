package teacher;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/** Teacher System: Dashboard -> Subjects -> Sections -> Students -> Grade Management. */
public class TeacherApp extends Application {

    // --- teacher profile (from the prototype) ---
    private static final String T_NAME = "Ronalyn Furio";
    private static final String T_ID = "211234";
    private static final String T_EMAIL = "ronalynfurio22@gmail.com";
    private static final String T_RANK = "Instructor I";
    private static final String T_DEPT = "College of Computing Multimedia Studies";

    private final GradeStore grades = new GradeStore();
    private final ObjectProperty<Image> photo = new SimpleObjectProperty<>(Ui.image("avatar"));

    private Stage stage;
    private ScrollPane scroll;
    private StackPane overlay;
    private Semester semester = Semester.FIRST;
    private GradeView currentGrade;          // non-null only while the grade page is open

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage stage) {
        this.stage = stage;

        scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("page-scroll");

        BorderPane layout = new BorderPane();
        layout.setLeft(sidebar());
        layout.setCenter(scroll);
        layout.getStyleClass().add("app-bg");

        overlay = new StackPane();
        overlay.setVisible(false);
        overlay.setManaged(false);

        StackPane root = new StackPane(layout, overlay);
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/teacher/styles.css").toExternalForm());

        stage.setTitle("Teacher System");
        stage.getIcons().add(Ui.image("logo"));
        stage.setMinWidth(1100);
        stage.setMinHeight(700);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            if (currentGrade != null && currentGrade.isDirty()) {
                e.consume();
                guard(Platform::exit);
            }
        });
        showSubjects();
        stage.show();
    }

    // =============================================================== chrome

    private Node sidebar() {
        VBox box = new VBox();
        box.getStyleClass().add("sidebar");
        box.setPrefWidth(256);
        box.setMinWidth(256);
        box.setMaxWidth(256);

        Label hello = Ui.label("Hello,\nMrs. Ronalyn", "hello");
        HBox hi = new HBox(10, Ui.avatar(46, photo), hello);
        hi.setAlignment(Pos.CENTER_LEFT);
        Label portal = Ui.label("Teacher Portal", "portal-pill");
        VBox who = new VBox(8, hi, portal);
        VBox.setMargin(portal, new Insets(0, 0, 0, 56));

        Button dash = new Button("Dashboard", Ui.icon("win_white", 22));
        dash.getStyleClass().add("nav-active");
        dash.setMaxWidth(Double.MAX_VALUE);
        dash.setGraphicTextGap(14);
        Ui.hand(dash);
        dash.setOnAction(e -> guard(this::showSubjects));

        Button logout = new Button("Logout", Ui.icon("logout", 26));
        logout.getStyleClass().add("logout");
        logout.setGraphicTextGap(12);
        Ui.hand(logout);
        logout.setOnAction(e -> guard(Platform::exit));   // the prototype has no login screen

        VBox top = new VBox(24, who, dash);
        VBox.setVgrow(top, javafx.scene.layout.Priority.ALWAYS);
        box.getChildren().addAll(top, logout);
        box.setPadding(new Insets(40, 25, 40, 14));
        return box;
    }

    private VBox header(Node back) {
        Label title = Ui.label("Teacher Dashboard", "h1");

        Label sub = Ui.label("School Year 2026-2027 \u2022 Editor Access \u2022 " + semester.label, "subtle-title");
        Label arrow = Ui.label("\u25BC", "semester-arrow");
        Ui.hand(arrow);
        arrow.setOnMouseClicked(e -> Ui.popup(arrow,
                List.of(Semester.FIRST.label, Semester.SECOND.label), semester.ordinal(),
                idx -> guard(() -> { semester = Semester.values()[idx]; showSubjects(); }),
                () -> arrow.setText("\u25B6"), () -> arrow.setText("\u25BC")));
        HBox subRow = new HBox(14, sub, arrow);
        subRow.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(6, title, subRow);
        if (back != null) left.getChildren().add(back);

        HBox top = new HBox(left, Ui.spacer(), teacherCard());
        top.setAlignment(Pos.TOP_LEFT);
        return new VBox(top);
    }

    private Node teacherCard() {
        Label name = Ui.label("Mrs. " + T_NAME, "tc-name");
        Label id = Ui.label("Teacher ID:  " + T_ID, "tc-id");
        Label rank = Ui.label(T_RANK, "tc-rank");
        HBox card = new HBox(12, Ui.avatar(68, photo), new VBox(3, name, id, rank));
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("teacher-card");
        card.setPrefWidth(285);
        card.setMinWidth(285);
        Ui.hand(card);
        card.setOnMouseClicked(e -> showProfile());
        return card;
    }

    private Button backButton(String text, Runnable action) {
        Button b = new Button("\u21A9  " + text);
        b.getStyleClass().add("back-btn");
        Ui.hand(b);
        b.setOnAction(e -> action.run());
        return b;
    }

    private void setPage(Node header, Node... body) {
        VBox page = new VBox(22);
        page.getStyleClass().add("page");
        VBox bodyBox = new VBox(12, body);
        page.getChildren().addAll(header, bodyBox);
        scroll.setContent(page);
        scroll.setVvalue(0);
    }

    private VBox sectionTitle(String title, String sub) {
        return new VBox(6, Ui.label(title, "h2"), Ui.label(sub, "h2-sub"));
    }

    // =============================================================== navigation guard + overlays

    /** Runs {@code proceed} immediately, or after the user confirms discarding unsaved grades. */
    private void guard(Runnable proceed) {
        if (currentGrade != null && currentGrade.isDirty()) {
            showUnsaved(proceed);
        } else {
            proceed.run();
        }
    }

    private void showOverlay(Node card, boolean dark) {
        Region dim = new Region();
        dim.getStyleClass().add(dark ? "dim" : "dim-light");
        overlay.getChildren().setAll(dim, card);
        StackPane.setAlignment(card, Pos.CENTER);
        overlay.setManaged(true);
        overlay.setVisible(true);
    }

    private void hideOverlay() {
        overlay.getChildren().clear();
        overlay.setVisible(false);
        overlay.setManaged(false);
    }

    private void showUnsaved(Runnable proceed) {
        Label msg = Ui.label("You have unsaved grade changes. Are you sure you want to leave this page "
                + "and discard your changes?", "dialog-text");
        msg.setWrapText(true);
        Button stay = new Button("Stay");
        stay.getStyleClass().add("btn-dark");
        stay.setOnAction(e -> hideOverlay());
        Button discard = new Button("Discard Changes");
        discard.getStyleClass().add("btn-primary-sm");
        discard.setOnAction(e -> { hideOverlay(); currentGrade = null; proceed.run(); });
        HBox buttons = new HBox(10, stay, discard);
        buttons.setAlignment(Pos.CENTER_RIGHT);
        VBox card = new VBox(12, Ui.label("Unsaved Changes", "dialog-title"), msg, buttons);
        card.getStyleClass().add("dialog");
        card.setMaxSize(500, Region.USE_PREF_SIZE);
        showOverlay(card, true);
    }

    private void showProfile() {
        Button close = new Button();
        close.setGraphic(Ui.icon("close", 18));
        close.getStyleClass().add("icon-button");
        close.setOnAction(e -> hideOverlay());
        Label title = Ui.label("Teacher Profile", "dialog-title-c");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        StackPane titleRow = new StackPane(title, close);
        StackPane.setAlignment(close, Pos.TOP_RIGHT);

        StackPane av = Ui.avatar(76, photo);
        Button plus = new Button();
        plus.setGraphic(Ui.icon("plus", 20));
        plus.getStyleClass().add("icon-button");
        Ui.hand(plus);
        plus.setOnAction(e -> choosePhoto());
        StackPane avatarBox = new StackPane(av, plus);
        StackPane.setAlignment(plus, Pos.BOTTOM_RIGHT);
        avatarBox.setMaxSize(88, 88);
        avatarBox.setPadding(new Insets(0, 6, 0, 6));

        // Labels are kept exactly as written in the prototype
        VBox card = new VBox(8, titleRow, avatarBox, Ui.label("Profile Photo", "profile-caption"));
        card.setAlignment(Pos.TOP_CENTER);
        addField(card, "Teacher\u2019s Name", T_NAME);
        addField(card, "Teacher\u2019s ID number", T_ID);
        addField(card, "Student\u2019s Email", T_EMAIL);
        addField(card, "Faculty Ranks", T_RANK);
        addField(card, "Department", T_DEPT);
        card.getStyleClass().add("profile-card");
        card.setPrefWidth(340);
        card.setMaxSize(340, Region.USE_PREF_SIZE);
        showOverlay(card, false);
        StackPane.setAlignment(card, Pos.TOP_CENTER);          // floats near the top, like the prototype
        StackPane.setMargin(card, new Insets(46, 0, 0, 0));
    }

    private void addField(VBox card, String caption, String value) {
        Label c = Ui.label(caption, "profile-label");
        c.setMaxWidth(Double.MAX_VALUE);
        TextField f = new TextField(value);
        f.setEditable(false);
        f.getStyleClass().add("profile-field");
        VBox box = new VBox(3, c, f);
        box.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(box, new Insets(6, 0, 0, 0));
        card.getChildren().add(box);
    }

    private void choosePhoto() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose profile photo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File f = fc.showOpenDialog(stage);
        if (f != null) photo.set(Ui.cropSquare(new Image(f.toURI().toString(), 400, 400, true, true)));
    }

    // =============================================================== screens

    private void showSubjects() {
        currentGrade = null;
        ListCard card = new ListCard("Subject List", "Your assigned subjects", "Search Subject",
                new double[]{6, 14, 52, 28}, "#", "Subject Code", "Subject Title", "Year Level");
        String[] query = {""};
        int[] year = {0};

        Runnable refresh = () -> {
            List<Subject> list = DataStore.subjects(semester).stream()
                    .filter(s -> year[0] == 0 || s.year() == year[0])
                    .filter(s -> matches(query[0], s.code(), s.title(), Ui.ordinalShort(s.year())))
                    .toList();
            card.setRows(rows(list, (s, n) -> new String[]{"" + n, s.code(), s.title(), Ui.ordinalShort(s.year())}),
                    i -> showSections(list.get(i)));
        };
        card.search.textProperty().addListener((o, a, b) -> { query[0] = b; refresh.run(); });
        card.filter.setOnAction(e -> Ui.popup(card.filter,
                List.of("First Year", "Second Year", "Third Year", "Fourth Year"), year[0] - 1,
                idx -> { year[0] = year[0] == idx + 1 ? 0 : idx + 1; refresh.run(); }, null, null));
        refresh.run();

        setPage(header(null), sectionTitle("Your Subjects", "Select a subject to view its enrolled students."), card);
    }

    private void showSections(Subject subject) {
        currentGrade = null;
        ListCard card = new ListCard("Section List",
                Ui.ordinalWord(subject.year()) + " \u2022 " + subject.code() + " \u2022 " + subject.title(),
                "Search Section", new double[]{6, 16, 14, 14, 20, 30},
                "#", "Program", "Year", "Block", "# of Student", "Action");
        String[] query = {""};
        int[] sort = {-1};   // -1 none, 0 high->low, 1 low->high

        Runnable refresh = () -> {
            List<Section> list = new java.util.ArrayList<>(DataStore.sections(subject).stream()
                    .filter(s -> matches(query[0], s.program(), "" + s.year(), s.block(), "" + s.studentCount()))
                    .toList());
            if (sort[0] == 0) list.sort((a, b) -> Integer.compare(b.studentCount(), a.studentCount()));
            if (sort[0] == 1) list.sort((a, b) -> Integer.compare(a.studentCount(), b.studentCount()));
            card.setRows(rows(list, (s, n) -> new String[]{"" + n, s.program(), "" + s.year(), s.block(),
                    "" + s.studentCount(), "View Students"}), i -> showStudents(list.get(i)));
        };
        card.search.textProperty().addListener((o, a, b) -> { query[0] = b; refresh.run(); });
        card.filter.setOnAction(e -> Ui.popup(card.filter,
                List.of("Highest to Lowest of Student", "Lowest to Highest of Student"), sort[0],
                idx -> { sort[0] = idx; refresh.run(); }, null, null));
        refresh.run();

        setPage(header(backButton("Back to Subjects", this::showSubjects)),
                sectionTitle("Select a Section", "Select a section to view the enrolled students."), card);
    }

    private void showStudents(Section section) {
        currentGrade = null;
        ListCard card = new ListCard("Student List",
                Ui.ordinalWord(section.year()) + " \u2022 " + section.subject().code() + " \u2022 "
                        + section.subject().title() + " \u2022 Block " + section.block(),
                "Search Student", new double[]{6, 18, 46, 30}, "#", "Student ID", "Student Name", "Action");
        String[] query = {""};
        int[] sort = {-1};   // 0 = A to Z, 1 = Z to A

        Runnable refresh = () -> {
            List<Student> list = new java.util.ArrayList<>(DataStore.students(section).stream()
                    .filter(s -> matches(query[0], s.id(), s.name()))
                    .toList());
            if (sort[0] == 0) list.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));
            if (sort[0] == 1) list.sort((a, b) -> b.name().compareToIgnoreCase(a.name()));
            card.setRows(rows(list, (s, n) -> new String[]{"" + n, s.id(), s.name(), "Manage Grades"}),
                    i -> showGrades(section, list.get(i), Period.MIDTERM));
        };
        card.search.textProperty().addListener((o, a, b) -> { query[0] = b; refresh.run(); });
        card.filter.setOnAction(e -> Ui.popup(card.filter, List.of("From A to Z", "From Z to A"), sort[0],
                idx -> { sort[0] = idx; refresh.run(); }, null, null));
        refresh.run();

        setPage(header(backButton("Back to Section", () -> showSections(section.subject()))),
                sectionTitle("Select a Student", "Choose a student to manage their grades"), card);
    }

    private void showGrades(Section section, Student student, Period period) {
        GradeView view = new GradeView(section, student, period, grades);
        currentGrade = view;

        Button midterm = periodButton("Midterm", period == Period.MIDTERM,
                () -> guard(() -> showGrades(section, student, Period.MIDTERM)));
        Button finals = periodButton("Finals", period == Period.FINALS,
                () -> guard(() -> showGrades(section, student, Period.FINALS)));
        HBox toggle = new HBox(0, midterm, finals);
        toggle.getStyleClass().add("segmented");

        HBox titleRow = new HBox(sectionTitle("Grade Management",
                "Enter the student\u2019s component scores for " + period.label), Ui.spacer(), toggle);
        titleRow.setAlignment(Pos.BOTTOM_LEFT);

        setPage(header(backButton("Back to Students", () -> guard(() -> showStudents(section)))), titleRow, view);
    }

    private Button periodButton(String text, boolean active, Runnable action) {
        Button b = new Button(text);
        b.getStyleClass().add(active ? "seg-active" : "seg");
        Ui.hand(b);
        b.setOnAction(e -> { if (!active) action.run(); });
        return b;
    }

    // =============================================================== small helpers

    private interface RowMaker<T> { String[] make(T item, int number); }

    private <T> List<String[]> rows(List<T> items, RowMaker<T> maker) {
        List<String[]> out = new java.util.ArrayList<>();
        for (int i = 0; i < items.size(); i++) out.add(maker.make(items.get(i), i + 1));
        return out;
    }

    private static boolean matches(String query, String... fields) {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return true;
        for (String f : fields) if (f.toLowerCase().contains(q)) return true;
        return false;
    }
}
