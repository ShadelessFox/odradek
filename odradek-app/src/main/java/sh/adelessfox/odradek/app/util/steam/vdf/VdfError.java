package sh.adelessfox.odradek.app.util.steam.vdf;

public record VdfError(String message, int offset) {
    @Override
    public String toString() {
        return message + " at offset " + offset;
    }
}
