package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.hfw.data.jolt.core.Factory;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class GroupFilter {
    public static GroupFilter sRestoreFromBinaryState(BinaryReader reader) throws IOException {
        var hash = reader.readInt();
        var name = Factory.getTypeName(hash);
        var result = switch (name) {
            case "GroupFilterTable" -> new GroupFilterTable();
            default -> throw new NotImplementedException();
        };
        result.restoreBinaryState(reader);
        return result;
    }

    public void restoreBinaryState(BinaryReader reader) throws IOException {
        // no-op
    }
}
