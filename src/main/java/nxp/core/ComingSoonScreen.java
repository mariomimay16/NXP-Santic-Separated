package nxp.core;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import static nxp.core.Ui.*;

/** Shown for a role whose portal has not been registered yet (e.g. TEACHER / ADMIN until you build them). */
public class ComingSoonScreen implements Screen {
    private final Router router; private final User user;
    public ComingSoonScreen(Router router, User user) { this.router = router; this.user = user; }
    @Override public Pane build(double dh) {
        Pane p = new Pane(); p.setStyle("-fx-background-color:linear-gradient(to bottom,#021a33,#00223b);");
        p.getChildren().addAll(ctext(user.role() + " portal is not available yet", 640, 380, 36, FontWeight.BOLD, Color.WHITE),
            ctext("Signed in as " + user.displayName(), 640, 425, 18, FontWeight.BOLD, DIM),
            rr(560, 470, 160, 44, 8, Color.web("#0066cc"), null), ctext("Logout", 640, 499, 18, FontWeight.BOLD, Color.WHITE),
            hit(560, 470, 160, 44, router::logout));
        return p;
    }
}
