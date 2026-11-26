package nxp.core;

import javafx.scene.layout.Pane;

/** One full-window screen/portal. Implement this for the Teacher and Admin portals too. */
public interface Screen {
    /** Builds the whole screen. {@code dh} = design height available (the Router scales the design to the window). */
    Pane build(double dh);
    /** Design width in design units (login = 1280, student = 1600). */
    default double designWidth() { return 1280; }
    /** true = fixed 1280x800 canvas centred in the window (login); false = fills the window width and scrolls. */
    default boolean fixedCanvas() { return false; }
}
