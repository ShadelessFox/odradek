package sh.adelessfox.odradek.lua;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public sealed interface LuaValue {
    record Nil() implements LuaValue {
        @Override
        public String toString() {
            return "nil";
        }
    }

    record Bool(boolean value) implements LuaValue {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record Num(int value) implements LuaValue {
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    record Str(String value) implements LuaValue {
        @Override
        public String toString() {
            return value;
        }
    }

    static LuaValue read(BinaryReader reader) throws IOException {
        var type = reader.readByte();
        return switch (type) {
            case 0 -> new Nil();
            case 1 -> new Bool(reader.readByteBoolean());
            case 3 -> new Num(reader.readInt());
            case 4 -> new Str(reader.readString(reader.readInt()));
            default -> throw new IOException("Bad constant type: " + type);
        };
    }
}
