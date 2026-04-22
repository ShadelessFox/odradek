package sh.adelessfox.odradek.app.util.steam.vdf;

import sh.adelessfox.odradek.parsing.Location;
import sh.adelessfox.odradek.parsing.Token;

sealed interface VdfToken extends Token {
    record Text(String value, Location location) implements VdfToken {
    }

    record Open(Location location) implements VdfToken {
    }

    record Close(Location location) implements VdfToken {
    }

    record End(Location location) implements VdfToken {
    }
}
