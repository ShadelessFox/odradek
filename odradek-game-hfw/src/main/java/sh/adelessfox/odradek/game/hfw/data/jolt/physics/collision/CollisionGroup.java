package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public final class CollisionGroup {
    public int groupId;
    public int subGroupId;
    public GroupFilter groupFilter;

    public static CollisionGroup restoreFromBinaryState(BinaryReader reader) throws IOException {
        CollisionGroup result = new CollisionGroup();
        result.groupId = reader.readInt();
        result.subGroupId = reader.readInt();

        return result;
    }
}
