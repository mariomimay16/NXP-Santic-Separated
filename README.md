# NXP Santic - Student Grade Management (JavaFX)
Run: `mvn javafx:run`  (JDK 17+, Maven)   Demo login: mariomimay12@gmail.com / mario123

## Structure (each part is separate)
```
nxp/Main.java                 entry point - registers which portal opens for each role
nxp/core/    Ui, Router, Screen, Role, User, ComingSoonScreen   (shared by every portal)
nxp/auth/    AuthService (credentials -> User+Role), LoginView  (the LOGIN system only)
nxp/student/ StudentApp, Grading, StudentData, Subj, StudentProfile (the STUDENT system only)
```
## Add the Teacher / Admin portal
1. Create `nxp/teacher/TeacherApp.java` -> `public class TeacherApp implements Screen` (copy StudentApp's shape: `build(double dh)`, `designWidth()`).
2. Add accounts in `AuthService.ACCOUNTS` with `Role.TEACHER` / `Role.ADMIN`.
3. In `Main.java` uncomment/add: `router.registerHome(Role.TEACHER, user -> new TeacherApp(router, user));`
Use `Router.render()` to refresh and `router.logout()` to sign out. Until a portal is registered, that role sees ComingSoonScreen.
