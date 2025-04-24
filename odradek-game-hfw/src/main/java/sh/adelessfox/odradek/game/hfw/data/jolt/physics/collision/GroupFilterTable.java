package sh.adelessfox.odradek.game.hfw.data.jolt.physics.collision;

import sh.adelessfox.odradek.game.hfw.data.jolt.JoltUtils;
import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;

public class GroupFilterTable extends GroupFilter {
    public int numSubGroups;
    public byte[] table;

    @Override
    public void restoreBinaryState(BinaryReader reader) throws IOException {
        super.restoreBinaryState(reader);

        numSubGroups = reader.readInt();
        table = JoltUtils.readBytes(reader);
    }
}
