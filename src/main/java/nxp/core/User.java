package nxp.core;

/** The signed-in account handed from the login system to a portal. */
public record User(String email, String displayName, Role role) {}
