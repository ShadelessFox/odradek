package sh.adelessfox.odradek.scene;

import wtf.reversed.toolbox.math.Matrix4;

import java.util.OptionalInt;

/**
 * Represents a bone in a skeleton.
 *
 * @param parent The index of the parent bone, or empty if this is the root bone.
 * @param name   The name of the bone.
 * @param matrix The transform of the bone relative to its parent.
 */
public record Bone(OptionalInt parent, String name, Matrix4 matrix) {
}
