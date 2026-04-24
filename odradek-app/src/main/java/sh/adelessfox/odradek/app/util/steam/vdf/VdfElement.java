package sh.adelessfox.odradek.app.util.steam.vdf;

import java.io.IOException;

public abstract sealed class VdfElement permits VdfObject, VdfString {
    public VdfObject getAsObject() {
        throw new IllegalStateException("Not an object: " + this);
    }

    public String getAsString() {
        throw new IllegalStateException("Not a string: " + this);
    }

    @Override
    public String toString() {
        try {
            var buf = new StringBuilder();
            VdfWriter.write(this, buf);
            return buf.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
