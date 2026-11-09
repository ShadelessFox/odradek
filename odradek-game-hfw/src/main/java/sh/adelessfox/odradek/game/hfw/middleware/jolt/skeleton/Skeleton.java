package sh.adelessfox.odradek.game.hfw.middleware.jolt.skeleton;

import sh.adelessfox.odradek.game.hfw.middleware.jolt.JoltUtils;
import wtf.reversed.toolbox.io.BinarySource;

import java.io.IOException;
import java.util.List;

public record Skeleton(List<Joint> joints) {
    public record Joint(String name, String parentName, int parentJointIndex) {
        private static Joint read(BinarySource source) throws IOException {
            var name = JoltUtils.readString(source);
            var parentJointIndex = source.readInt();
            var parentName = JoltUtils.readString(source);

            return new Joint(name, parentName, parentJointIndex);
        }
    }

    public static Skeleton restoreFromBinaryState(BinarySource source) throws IOException {
        var joints = source.readObjects(source.readInt(), Joint::read);

        return new Skeleton(joints);
    }
}
