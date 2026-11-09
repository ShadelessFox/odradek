package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision;

import sh.adelessfox.odradek.NotImplementedException;
import sh.adelessfox.odradek.game.hfw.middleware.jolt.core.Factory;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class GroupFilter {
    public static GroupFilter sRestoreFromBinaryState(BinarySource source) throws IOException {
        var hash = source.readInt();
        var name = Factory.getTypeName(hash);
        var result = switch (name) {
            case "GroupFilterTable" -> new GroupFilterTable();
            default -> throw new NotImplementedException();
        };
        result.restoreBinaryState(source);
        return result;
    }

    public void restoreBinaryState(BinarySource source) throws IOException {
        // no-op
    }
}
