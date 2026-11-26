package nxp;

import javafx.application.Application;
import javafx.stage.Stage;
import nxp.auth.LoginView;
import nxp.core.Role;
import nxp.core.Router;
import nxp.student.StudentApp;

/** Entry point. Registers one portal per role; the login system decides which one opens. */
public class Main extends Application {
    @Override public void start(Stage stage) {
        Router router = new Router(stage).setLoginScreen(LoginView::new);
        router.registerHome(Role.STUDENT, user -> new StudentApp(router, user));
        // router.registerHome(Role.TEACHER, user -> new TeacherApp(router, user));   // <- add your teacher portal here
        // router.registerHome(Role.ADMIN,   user -> new AdminApp(router, user));     // <- and the admin portal here
        router.start();
    }
    public static void main(String[] args) { launch(args); }
}
