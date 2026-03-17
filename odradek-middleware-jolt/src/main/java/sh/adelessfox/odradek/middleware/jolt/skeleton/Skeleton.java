package sh.adelessfox.odradek.middleware.jolt.skeleton;

import sh.adelessfox.odradek.io.BinaryReader;
import sh.adelessfox.odradek.middleware.jolt.JoltUtils;

import java.io.IOException;
import java.util.List;

public record Skeleton(List<Joint> joints) {
    public record Joint(String name, String parentName, int parentJointIndex) {
        private static Joint read(BinaryReader reader) throws IOException {
            var name = JoltUtils.readString(reader);
            var parentJointIndex = reader.readInt();
            var parentName = JoltUtils.readString(reader);

            return new Joint(name, parentName, parentJointIndex);
        }
    }

    public static Skeleton restoreFromBinaryState(BinaryReader reader) throws IOException {
        var joints = reader.readObjects(reader.readInt(), Joint::read);

        return new Skeleton(joints);
    }
}
