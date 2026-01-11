package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.math.Matrix4f;

@FunctionalInterface
public interface NodeVisitor {
    /**
     * Visits a node.
     *
     * @param node      the node to visit
     * @param transform the accumulated transform
     * @return {@code true} to continue visiting child nodes, {@code false} to skip them
     */
    boolean visit(Node node, Matrix4f transform);
}
