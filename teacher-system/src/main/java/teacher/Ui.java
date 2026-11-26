package teacher;

import javafx.beans.property.ObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/** Small UI helpers shared by all screens. */
final class Ui {
    private Ui() {}

    private static final Map<String, Image> CACHE = new HashMap<>();

    /** Loads /teacher/icons/<name>.png (the icons exported from the prototype SVG). */
    static Image image(String name) {
        return CACHE.computeIfAbsent(name,
                n -> new Image(Ui.class.getResourceAsStream("/teacher/icons/" + n + ".png")));
    }

    static ImageView icon(String name, double width) {
        ImageView v = new ImageView(image(name));
        v.setFitWidth(width);
        v.setPreserveRatio(true);
        v.setSmooth(true);
        return v;
    }

    static Label label(String text, String... styleClasses) {
        Label l = new Label(text);
        l.getStyleClass().addAll(styleClasses);
        return l;
    }

    /** Horizontal spacer: pushes the following nodes of an HBox to the right. */
    static Region spacer() {
        Region r = new Region();
        javafx.scene.layout.HBox.setHgrow(r, Priority.ALWAYS);
        return r;
    }

    /** Round avatar bound to the shared profile photo. */
    static StackPane avatar(double size, ObjectProperty<Image> photo) {
        ImageView iv = new ImageView();
        iv.imageProperty().bind(photo);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(false);
        iv.setSmooth(true);
        iv.setClip(new Circle(size / 2, size / 2, size / 2));
        StackPane p = new StackPane(iv);
        p.setMinSize(size, size);
        p.setMaxSize(size, size);
        return p;
    }

    /** Centre-crops an image to a square so it fits the round avatar. */
    static Image cropSquare(Image src) {
        int w = (int) src.getWidth(), h = (int) src.getHeight();
        int s = Math.min(w, h);
        PixelReader pr = src.getPixelReader();
        if (pr == null || s <= 0) return src;
        return new WritableImage(pr, (w - s) / 2, (h - s) / 2, s, s);
    }

    static void roundClip(Region r, double radius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(radius * 2);
        clip.setArcHeight(radius * 2);
        clip.widthProperty().bind(r.widthProperty());
        clip.heightProperty().bind(r.heightProperty());
        r.setClip(clip);
    }

    /** Dark drop-down used for semester / filter / sort menus. */
    static ContextMenu popup(Node anchor, List<String> options, int selected, IntConsumer onPick,
                             Runnable onShow, Runnable onHide) {
        ContextMenu menu = new ContextMenu();
        for (int i = 0; i < options.size(); i++) {
            final int idx = i;
            MenuItem item = new MenuItem(options.get(i));
            if (i == selected) item.getStyleClass().add("menu-item-selected");
            item.setOnAction(e -> onPick.accept(idx));
            menu.getItems().add(item);
        }
        if (onShow != null) menu.setOnShowing(e -> onShow.run());
        if (onHide != null) menu.setOnHidden(e -> onHide.run());
        menu.show(anchor, Side.BOTTOM, 0, 4);
        return menu;
    }

    static String ordinalShort(int n) {
        return switch (n) { case 1 -> "1st Year"; case 2 -> "2nd Year"; case 3 -> "3rd Year"; default -> n + "th Year"; };
    }

    static String ordinalWord(int n) {
        return switch (n) { case 1 -> "First Year"; case 2 -> "Second Year"; case 3 -> "Third Year"; default -> "Fourth Year"; };
    }

    static void hand(Node n) { n.setCursor(Cursor.HAND); }

    static Insets pad(double top, double right, double bottom, double left) { return new Insets(top, right, bottom, left); }

    static Pos center() { return Pos.CENTER; }
}
