package sh.adelessfox.odradek.game.hfw.middleware.jolt.physics.collision;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;

public class GroupFilterTable extends GroupFilter {
    public int numSubGroups;
    public Bytes table;

    @Override
    public void restoreBinaryState(BinarySource source) throws IOException {
        super.restoreBinaryState(source);

        numSubGroups = source.readInt();
        table = JoltUtils.readBytes(source);
    }
}
