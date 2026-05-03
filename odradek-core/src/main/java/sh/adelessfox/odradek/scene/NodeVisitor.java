package sh.adelessfox.odradek.scene;

import wtf.reversed.toolbox.math.Matrix4;

@FunctionalInterface
public interface NodeVisitor {
    /**
     * Visits a node.
     *
     * @param node      the node to visit
     * @param transform the accumulated transform
     * @return {@code true} to continue visiting child nodes, {@code false} to skip them
     */
    boolean visit(Node node, Matrix4 transform);
}
