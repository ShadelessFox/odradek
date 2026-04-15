package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.hashing.HashCode;

import java.util.OptionalInt;

public interface LinkTable {
    record Result(int position, OptionalInt group, int index) {
    }

    Result read(int position);

    HashCode checksum();
}
