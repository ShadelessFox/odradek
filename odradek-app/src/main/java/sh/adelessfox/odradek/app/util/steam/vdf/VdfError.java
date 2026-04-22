package sh.adelessfox.odradek.app.util.steam.vdf;

import sh.adelessfox.odradek.parsing.Location;

public record VdfError(String message, Location location) {
    @Override
    public String toString() {
        return message + " at " + location;
    }
}
