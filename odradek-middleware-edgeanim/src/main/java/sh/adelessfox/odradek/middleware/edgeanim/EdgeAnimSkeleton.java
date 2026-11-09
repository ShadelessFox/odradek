package sh.adelessfox.odradek.middleware.edgeanim;

import wtf.reversed.toolbox.io.BinarySource;

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

    public static EdgeAnimSkeleton read(BinarySource source) throws IOException {
        var tag = source.readInt();
        if (tag != TAG) {
            throw new IOException("Invalid EdgeAnimSkeleton tag: " + tag);
        }
        var sizeTotal = source.readInt();
        var sizeCustomData = source.readInt();
        var sizeNameHashes = source.readInt();
        var numJoints = source.readShort();
        var numUserChannels = source.readShort();
        var flags = source.readShort();
        var locomotionJointIndex = source.readShort();
        var offsetBasePose = source.readInt();
        var offsetJointLinkageMap = source.readInt();
        var offsetJointNameHashArray = source.readInt();
        var offsetUserChannelNameHashArray = source.readInt();
        var offsetUserChannelNodeNameHashArray = source.readInt();
        var offsetUserChannelFlagsArray = source.readInt();
        var offsetCustomData = source.readInt();
        source.skip(8);
        var numJointLinkages = source.readInt();

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

    public List<EdgeAnimJointTransform> readBasePose(BinarySource source) throws IOException {
        source.position(offsetBasePose + 24);
        return source.readObjects(numJoints, EdgeAnimJointTransform::read);
    }
}
