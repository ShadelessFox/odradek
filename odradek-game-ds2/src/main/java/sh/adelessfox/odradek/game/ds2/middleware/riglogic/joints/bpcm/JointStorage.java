package sh.adelessfox.odradek.game.ds2.middleware.riglogic.joints.bpcm;

import wtf.reversed.toolbox.collect.Shorts;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public record JointStorage(
    Shorts values,
    Shorts inputIndices,
    Shorts outputIndices,
    List<LODRegion> lodRegions,
    List<JointGroup> jointGroups
) {
    public static JointStorage read(BinarySource source) throws IOException {
        return new JointStorage(
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readShorts(source.readInt()),
            source.readObjects(source.readInt(), LODRegion::read),
            source.readObjects(source.readInt(), JointGroup::read)
        );
    }
}
