package nxp.auth;

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
import nxp.core.*;
import static nxp.core.Ui.*;

/** The login screen only. Knows nothing about students/teachers/admins - it just asks AuthService and hands the User to the Router. */
public class LoginView implements Screen {
    private final Router router;
    public LoginView(Router router) { this.router = router; }
    @Override public boolean fixedCanvas() { return true; }

    boolean pwVisible, emBad, pwBad; String emVal = "", pwVal = "";
    @Override public Pane build(double dh) {
        Pane p = new Pane(); p.setStyle("-fx-background-color:linear-gradient(from 0% 0% to 60% 60%,#000000 0%,#071a38 40%,#081a37 100%);");
        ImageView bg = iv("bglogo", -60, -50); bg.setOpacity(.55); bg.setEffect(new GaussianBlur(2)); p.getChildren().add(bg);
        Rectangle card = rr(103, 72, 495, 655, 60, Color.rgb(255, 255, 255, .08), Color.rgb(255, 255, 255, .6)); card.setStrokeWidth(3);
        card.setEffect(new DropShadow(30, Color.rgb(80, 140, 255, .35)));
        p.getChildren().addAll(card, ctext("Login", 350, 152, 54, FontWeight.BOLD, Color.WHITE),
            ctext("Please sign in to access grade", 350, 228, 30, FontWeight.NORMAL, Color.web("#e4ecf6")), ctext("management", 350, 273, 30, FontWeight.NORMAL, Color.web("#e4ecf6")),
            text("Email", 137, 340, 28, FontWeight.NORMAL, Color.WHITE), text("Password", 137, 463, 28, FontWeight.NORMAL, Color.WHITE));
        Rectangle ef = rr(134, 363, 435, 54, 6, emBad ? Color.WHITE : Color.WHITE, emBad ? Color.RED : Color.web("#222")); Rectangle pf = rr(134, 494, 428, 54, 6, Color.WHITE, pwBad ? Color.RED : Color.web("#222"));
        TextField em = new TextField(emVal); em.setLayoutX(190); em.setLayoutY(366); em.setPrefSize(370, 48);
        PasswordField pw = new PasswordField(); pw.setText(pwVal); pw.setLayoutX(190); pw.setLayoutY(497); pw.setPrefSize(310, 48);
        TextField pws = new TextField(pwVal); pws.setLayoutX(190); pws.setLayoutY(497); pws.setPrefSize(310, 48);
        pws.textProperty().bindBidirectional(pw.textProperty()); pws.setVisible(pwVisible); pw.setVisible(!pwVisible);
        String fs = "-fx-background-color:transparent;-fx-font-size:21px;-fx-font-family:'Inter';-fx-text-fill:#222;-fx-prompt-text-fill:#777;-fx-background-insets:0;-fx-focus-color:transparent;-fx-faint-focus-color:transparent;";
        String bad = fs.replace("#222", "#ff0000").replace("#777", "#ff0000");
        em.setPromptText(emBad ? "Email is invalid" : "Email"); pw.setPromptText(pwBad ? "Password is invalid" : "Password"); pws.setPromptText(pw.getPromptText());
        em.setStyle(emBad ? bad : fs); pw.setStyle(pwBad ? bad : fs); pws.setStyle(pwBad ? bad : fs);
        ImageView ui = iv(emBad ? "u_red" : "u_gray", 144, 371), li = iv(pwBad ? "l_red" : "l_gray", 147, 501), eye = iv(pwVisible ? "eye_off" : "eye", 512, 513);
        Button eb = hit(505, 500, 50, 40, () -> { emVal = em.getText(); pwVal = pw.getText(); pwVisible = !pwVisible; router.render(); });
        Rectangle btn = rr(248, 604, 207, 54, 6, Color.web("#0066cc"), Color.web("#1f8fff")); btn.setEffect(new DropShadow(22, Color.rgb(60, 140, 255, .8)));
        Runnable submit = () -> {
            String e = em.getText().trim(), pwd = pw.getText();
            boolean eo = AuthService.isValidEmail(e), po = !pwd.isEmpty();
            Optional<User> auth = eo && po ? AuthService.authenticate(e, pwd) : Optional.empty();
            if (auth.isPresent()) { emBad = pwBad = false; emVal = pwVal = ""; router.loginSucceeded(auth.get()); return; }
            emBad = !eo || po; pwBad = !po || eo; emVal = emBad ? "" : e; pwVal = ""; router.render();
        };
        Button lb = hit(248, 604, 207, 54, submit); pw.setOnAction(a -> submit.run()); em.setOnAction(a -> submit.run()); pws.setOnAction(a -> submit.run());
        p.getChildren().addAll(ef, pf, ui, li, em, pw, pws, eye, eb, btn, ctext("Login", 351, 640, 28, FontWeight.BOLD, Color.WHITE), lb);
        ImageView logo = iv("logo", 777, 114); logo.setFitWidth(320); logo.setFitHeight(320); logo.setEffect(new DropShadow(30, Color.rgb(0, 120, 255, .5)));
        p.getChildren().addAll(logo, ctext("NXP Santic", 937, 506, 44, FontWeight.BOLD, Color.WHITE), ctext("Student Grade Management", 937, 560, 29, FontWeight.NORMAL, Color.WHITE));
        return p;
    }
}
