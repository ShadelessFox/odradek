package sh.adelessfox.odradek.game.hfw.data.riglogic.joints.bpcm;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public record JointStorage(
    short[] values,
    short[] inputIndices,
    short[] outputIndices,
    List<LODRegion> lodRegions,
    List<JointGroup> jointGroups
) {
    public static JointStorage read(BinaryReader reader) throws IOException {
        return new JointStorage(
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt()),
            reader.readShorts(reader.readInt()),
            reader.readObjects(reader.readInt(), LODRegion::read),
            reader.readObjects(reader.readInt(), JointGroup::read)
        );
    }
}
