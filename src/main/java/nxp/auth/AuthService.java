package nxp.auth;

import nxp.core.Role;
import nxp.core.User;
import java.util.Map;
import java.util.Optional;

/** The login SYSTEM: validates credentials and returns the User (with a Role). Swap the map for a database later. */
public final class AuthService {
    private AuthService() {}
    private record Account(String password, User user) {}
    // email (lower-case) -> account.  Add teacher/admin accounts here when their portals exist.
    private static final Map<String, Account> ACCOUNTS = Map.of(
        "mariomimay12@gmail.com", new Account("mario123", new User("mariomimay12@gmail.com", "Mario A. Mimay", Role.STUDENT)));

    public static boolean isValidEmail(String e) { return e.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"); }
    public static Optional<User> authenticate(String email, String password) {
        Account a = ACCOUNTS.get(email.trim().toLowerCase());
        return a != null && a.password().equals(password) ? Optional.of(a.user()) : Optional.empty();
    }
}
