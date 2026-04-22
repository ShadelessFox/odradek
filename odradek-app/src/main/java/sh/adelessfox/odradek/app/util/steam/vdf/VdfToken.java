package sh.adelessfox.odradek.app.util.steam.vdf;

sealed interface VdfToken {
    int offset();

    record Text(String value, int offset) implements VdfToken {
    }

    record Open(int offset) implements VdfToken {
    }

    record Close(int offset) implements VdfToken {
    }

    record End(int offset) implements VdfToken {
    }
}
