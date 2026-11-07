package sh.adelessfox.odradek.game.ds2.middleware.jolt.physics.collision;

import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public final class CollisionGroup {
    public int groupId;
    public int subGroupId;
    public GroupFilter groupFilter;

    public static CollisionGroup restoreFromBinaryState(BinarySource source) throws IOException {
        CollisionGroup result = new CollisionGroup();
        result.groupId = source.readInt();
        result.subGroupId = source.readInt();

        return result;
    }
}
