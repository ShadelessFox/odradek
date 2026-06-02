package sh.adelessfox.odradek.scene;

import java.util.List;

public record Skeleton(List<Bone> bones) {
    public Skeleton {
        validateBones(bones);
        bones = List.copyOf(bones);
    }

    private static void validateBones(List<Bone> bones) {
        for (int i = 0; i < bones.size(); i++) {
            var bone = bones.get(i);
            if (bone.parent().isEmpty()) {
                continue;
            }
            int parent = bone.parent().getAsInt();
            if (parent < 0 || parent >= i) {
                throw new IllegalArgumentException("Bone " + i + " has an invalid parent index " + parent);
            }
        }
    }
}
