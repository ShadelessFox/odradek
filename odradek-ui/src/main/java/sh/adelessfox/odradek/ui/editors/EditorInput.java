package sh.adelessfox.odradek.ui.editors;

/**
 * An input for an {@link Editor}
 */
public interface EditorInput {
    /**
     * The name of this input
     */
    String getName();

    /**
     * The description of this input
     */
    String getDescription();

    /**
     * Checks whether two editor inputs represent the same input. The other
     * input is not necessary of the same type as this input.
     *
     * @param other other editor input
     * @return {@code true} if both inputs represent the same input, {@code false} otherwise
     */
    boolean representsSameInput(EditorInput other);
}
