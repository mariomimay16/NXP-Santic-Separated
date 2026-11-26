package nxp.core;

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

/** Shared drawing helpers + design colours (used by EVERY portal: login, student, teacher, admin). */
public final class Ui {
    private Ui() {}
    // design colours sampled from the prototype
    public static final Color DIM = Color.web("#8fa8ba"), TXT = Color.web("#9db3c4"), CY = Color.web("#00d0ff"), LINE = Color.web("#193a5b"), CARD = Color.web("#00223b");

    // ---------- helpers ----------
    public static Image img(String n) { return new Image(Ui.class.getResourceAsStream("/a/" + n + ".png")); }
    public static ImageView iv(String n, double x, double y) { ImageView v = new ImageView(img(n)); v.setLayoutX(x); v.setLayoutY(y); return v; }
    /** places an asset so that its visible "core" starts exactly at (x,y), as in the prototype */
    public static final Map<String, double[]> OFF = new HashMap<>();
    static {
        double[][] d = {{10,13.9},{11.3,14.1},{14,14},{13.9,13.9},{4,4},{4,4},{4,4},{4.3,4.3},{4,4},{4.1,4.1},{4.2,4.2},{4.1,4.1},{9.9,9.9},{10.1,10.1},{9.7,9.7},{.3,.3},{.4,.4},{.3,.3},{.2,.2},{.3,.3},{1.2,1.2}};
        String[] n = {"nav_dash_on","nav_grades_on","nav_dash_off","nav_grades_off","ic_medal","ic_book","ic_shield","ic_clock","ic_x","b_pass","b_pend","b_fail","big_pass","big_pend","big_fail","ic_logout","ic_plus","ic_close","ic_search","ic_filter","avatar"};
        for (int i = 0; i < n.length; i++) OFF.put(n[i], d[i]);
    }
    public static ImageView at(String n, double x, double y) { double[] o = OFF.getOrDefault(n, new double[]{0, 0}); return iv(n, x - o[0], y - o[1]); }
    public static Text text(String s, double x, double baseline, double size, FontWeight w, Color c) {
        Text t = new Text(s); t.setFont(Font.font("Inter", w, size)); t.setFill(c); t.setX(x); t.setY(baseline); return t;
    }
    public static Text ctext(String s, double cx, double baseline, double size, FontWeight w, Color c) {
        Text t = text(s, 0, baseline, size, w, c); t.setX(cx - t.getLayoutBounds().getWidth() / 2); return t;
    }
    public static Rectangle rr(double x, double y, double w, double h, double r, Paint fill, Paint stroke) {
        Rectangle e = new Rectangle(x, y, w, h); e.setArcWidth(r * 2); e.setArcHeight(r * 2); e.setFill(fill); e.setStroke(stroke); return e;
    }
    public static Button hit(double x, double y, double w, double h, Runnable r) {
        Button b = new Button(); b.setLayoutX(x); b.setLayoutY(y); b.setPrefSize(w, h); b.setStyle("-fx-background-color:transparent;-fx-cursor:hand;"); b.setOnAction(e -> r.run()); return b;
    }
    public static Polygon arrow(double cx, double cy, boolean down, double size, Color c) {
        Polygon p = down ? new Polygon(cx - size, cy - size * .6, cx + size, cy - size * .6, cx, cy + size * .7)
                         : new Polygon(cx - size * .6, cy - size, cx - size * .6, cy + size, cx + size * .7, cy); p.setFill(c); return p;
    }
}
