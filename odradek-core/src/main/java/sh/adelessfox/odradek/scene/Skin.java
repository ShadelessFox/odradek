package sh.adelessfox.odradek.scene;

import java.util.List;

public record Skin(List<Joint> joints) {
    public Skin {
        validateJoints(joints);
        joints = List.copyOf(joints);
    }

    private static void validateJoints(List<Joint> joints) {
        for (int i = 0; i < joints.size(); i++) {
            var joint = joints.get(i);
            if (joint.parent().isEmpty()) {
                continue;
            }
            int parent = joint.parent().getAsInt();
            if (parent < 0 || parent >= i) {
                throw new IllegalArgumentException("Joint " + i + " has an invalid parent index " + parent);
            }
        }
    }
}
