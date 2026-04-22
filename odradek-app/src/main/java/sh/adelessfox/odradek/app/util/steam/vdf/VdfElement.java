package sh.adelessfox.odradek.app.util.steam.vdf;

public sealed interface VdfElement permits VdfObject, VdfString {
    default VdfObject getAsObject() {
        return switch (this) {
            case VdfObject o -> o;
            default -> throw new IllegalStateException("Not an object: " + this);
        };
    }

    default String getAsString() {
        return switch (this) {
            case VdfString s -> s.toString();
            default -> throw new IllegalStateException("Not a string: " + this);
        };
    }
}
