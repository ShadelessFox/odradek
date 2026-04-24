package sh.adelessfox.odradek.app.util.steam.vdf;

import java.util.Objects;

public final class VdfString extends VdfElement {
    private final String value;

    public VdfString(String value) {
        this.value = value;
    }

    @Override
    public String getAsString() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof VdfString vdfString
            && Objects.equals(value, vdfString.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
