package sh.adelessfox.odradek.game.hfw.data.edge;

import sh.adelessfox.odradek.io.BinaryReader;

import java.io.IOException;
import java.util.List;

public record EdgeAnimSkeleton(
    int sizeTotal,
    int sizeCustomData,
    int sizeNameHashes,
    short numJoints,
    short numUserChannels,
    short flags,
    short locomotionJointIndex,
    int offsetBasePose,
    int offsetJointLinkageMap,
    int offsetJointNameHashArray,
    int offsetUserChannelNameHashArray,
    int offsetUserChannelNodeNameHashArray,
    int offsetUserChannelFlagsArray,
    int offsetCustomData,
    int numJointLinkages
) {
    private static final int TAG = 'E' << 24 | 'S' << 16 | '0' << 8 | '6';

    public static EdgeAnimSkeleton read(BinaryReader reader) throws IOException {
        var tag = reader.readInt();
        if (tag != TAG) {
            throw new IOException("Invalid EdgeAnimSkeleton tag: " + tag);
        }
        var sizeTotal = reader.readInt();
        var sizeCustomData = reader.readInt();
        var sizeNameHashes = reader.readInt();
        var numJoints = reader.readShort();
        var numUserChannels = reader.readShort();
        var flags = reader.readShort();
        var locomotionJointIndex = reader.readShort();
        var offsetBasePose = reader.readInt();
        var offsetJointLinkageMap = reader.readInt();
        var offsetJointNameHashArray = reader.readInt();
        var offsetUserChannelNameHashArray = reader.readInt();
        var offsetUserChannelNodeNameHashArray = reader.readInt();
        var offsetUserChannelFlagsArray = reader.readInt();
        var offsetCustomData = reader.readInt();
        reader.skip(8);
        var numJointLinkages = reader.readInt();

        return new EdgeAnimSkeleton(
            sizeTotal,
            sizeCustomData,
            sizeNameHashes,
            numJoints,
            numUserChannels,
            flags,
            locomotionJointIndex,
            offsetBasePose,
            offsetJointLinkageMap,
            offsetJointNameHashArray,
            offsetUserChannelNameHashArray,
            offsetUserChannelNodeNameHashArray,
            offsetUserChannelFlagsArray,
            offsetCustomData,
            numJointLinkages
        );
    }

    public List<EdgeAnimJointTransform> readBasePose(BinaryReader reader) throws IOException {
        reader.position(offsetBasePose + 24);
        return reader.readObjects(numJoints, EdgeAnimJointTransform::read);
    }
}
