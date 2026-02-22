package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.math.Matrix4f;

import java.util.OptionalInt;

/**
 * Represents a joint of a skin.
 *
 * @param parent The index of the parent joint, or empty if this is the root joint.
 * @param name   The name of the joint.
 * @param matrix The transform of the joint relative to its parent.
 */
public record Joint(OptionalInt parent, String name, Matrix4f matrix) {
}
