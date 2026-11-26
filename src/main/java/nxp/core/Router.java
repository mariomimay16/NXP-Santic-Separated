package nxp.core;

import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/** Owns the window, scales the current Screen to fit, and routes login -> the portal for the user's role. */
public class Router {
    private final Stage stage;
    private final StackPane host = new StackPane();
    private final Map<Role, Function<User, Screen>> homes = new EnumMap<>(Role.class);
    private Function<Router, Screen> loginFactory;
    private Screen current;
    private User user;

    public Router(Stage stage) { this.stage = stage; }

    /** Register the portal that opens after login for a role. */
    public Router registerHome(Role role, Function<User, Screen> portal) { homes.put(role, portal); return this; }
    /** Register the login screen (kept outside core so the login system stays a separate module). */
    public Router setLoginScreen(Function<Router, Screen> factory) { this.loginFactory = factory; return this; }
    public User user() { return user; }

    public void start() {
        stage.setTitle("NXP Santic - Student Grade Management");
        host.setStyle("-fx-background-color:#06172f;");
        Scene sc = new Scene(host, 1280, 800);
        sc.getStylesheets().add(Router.class.getResource("/style.css").toExternalForm());
        stage.getIcons().add(Ui.img("logo"));
        stage.setScene(sc); stage.setMinWidth(900); stage.setMinHeight(520);
        host.widthProperty().addListener(o -> render()); host.heightProperty().addListener(o -> render());
        showLogin(); stage.show();
    }
    public void showLogin() { user = null; current = loginFactory.apply(this); render(); }
    public void loginSucceeded(User u) {
        user = u; Function<User, Screen> f = homes.get(u.role());
        current = f != null ? f.apply(u) : new ComingSoonScreen(this, u); render();
    }
    public void logout() { showLogin(); }

    /** Rebuilds the current screen (call after any state change). */
    public void render() {
        double W = host.getWidth(), H = host.getHeight(); if (W <= 0 || H <= 0 || current == null) return;
        boolean fixed = current.fixedCanvas(); double dw = current.designWidth();
        double k = fixed ? Math.min(W / dw, H / 800) : W / dw, dh = fixed ? 800 : H / k;
        Pane d = current.build(dh);
        d.setPrefSize(dw, dh); d.setMinSize(dw, dh); d.setMaxSize(dw, dh);
        d.setLayoutX((W - dw * k) / 2); d.setLayoutY((H - dh * k) / 2);
        d.getTransforms().setAll(new Scale(k, k, 0, 0));
        Pane root = new Pane(d); root.setPrefSize(W, H); root.setMaxSize(W, H); host.getChildren().setAll(root);
    }
}
