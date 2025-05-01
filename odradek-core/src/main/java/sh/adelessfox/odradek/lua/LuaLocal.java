package sh.adelessfox.odradek.lua;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public record LuaLocal(String name, int start, int end) {
    public static LuaLocal read(BinaryReader reader) throws IOException {
        var name = reader.readString(reader.readInt());
        var start = reader.readInt();
        var end = reader.readInt();
        return new LuaLocal(name, start, end);
    }
}
