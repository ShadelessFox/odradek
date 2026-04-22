package sh.adelessfox.odradek.app.util.steam.vdf;

public final class VdfString implements VdfElement {
    private final String value;

    public VdfString(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
