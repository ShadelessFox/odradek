package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Model;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;

import java.util.*;
import java.util.stream.Stream;

/**
 * A node in a scene graph.
 *
 * @param name     an optional name for the node
 * @param model    an optional model to render at this node. Can be shared with other nodes to achieve instancing.
 * @param skeleton an optional skeleton in this node's local coordinate space, used for skinning the {@code model}
 *                 when present. May exist without a model and be shared with other nodes. Not inherited by child nodes.
 * @param children child nodes of this node
 * @param matrix   the transformation matrix of this node relative to its parent
 */
public record Node(
    Optional<String> name,
    Optional<Model> model,
    Optional<Skeleton> skeleton,
    List<Node> children,
    Matrix4 matrix
) {
    public Node {
        children = List.copyOf(children);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Optional<Node> of(List<Node> children) {
        if (children.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Node(
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            children,
            Matrix4.IDENTITY));
    }

    public static Node of(Model model) {
        return new Node(Optional.empty(), Optional.of(model), Optional.empty(), List.of(), Matrix4.IDENTITY);
    }

    public Node add(Node child) {
        var children = new ArrayList<>(this.children);
        children.add(child);
        return new Node(name, model, skeleton, children, matrix);
    }

    public Node transform(Matrix4 transform) {
        return new Node(name, model, skeleton, children, matrix.multiply(transform));
    }

    public void accept(NodeVisitor visitor) {
        accept(visitor, matrix);
    }

    private void accept(NodeVisitor visitor, Matrix4 transform) {
        if (visitor.visit(this, transform)) {
            for (var child : children) {
                child.accept(visitor, transform.multiply(child.matrix));
            }
        }
    }

    /**
     * Computes bounds of this node and its descendants in this node's parent space.
     * The result already includes this node's matrix.
     */
    public Optional<Bounds> computeBounds() {
        return computeBounds(Matrix4.IDENTITY);
    }

    private Optional<Bounds> computeBounds(Matrix4 parentTransform) {
        var transform = parentTransform.multiply(matrix);
        var bbox1 = model.stream()
            .map(model -> model.computeBounds(transform))
            .flatMap(Optional::stream);

        var bbox2 = children.stream()
            .map(child -> child.computeBounds(transform))
            .flatMap(Optional::stream);

        return Stream.concat(bbox1, bbox2)
            .reduce(Bounds::combine);
    }

    public static final class Builder {
        record NodeOrBuilder(Node node, Builder builder) {
            static NodeOrBuilder of(Node node) {
                Objects.requireNonNull(node, "node");
                return new NodeOrBuilder(node, null);
            }

            static NodeOrBuilder of(Builder builder) {
                Objects.requireNonNull(builder, "builder");
                return new NodeOrBuilder(null, builder);
            }

            Node toNode() {
                return node != null ? node : builder.build();
            }
        }

        private final List<NodeOrBuilder> children = new ArrayList<>();
        private String name;
        private Model model;
        private Skeleton skeleton;
        private Matrix4 matrix = Matrix4.IDENTITY;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder model(Model model) {
            this.model = model;
            return this;
        }

        public Builder skeleton(Skeleton skeleton) {
            this.skeleton = skeleton;
            return this;
        }

        public Matrix4 matrix() {
            return matrix;
        }

        public Builder matrix(Matrix4 matrix) {
            this.matrix = matrix;
            return this;
        }

        public Builder children(Collection<Node> children) {
            this.children.clear();
            for (Node child : children) {
                this.children.add(NodeOrBuilder.of(child));
            }
            return this;
        }

        public Builder add(Node child) {
            children.add(NodeOrBuilder.of(child));
            return this;
        }

        public Builder add(Builder child) {
            children.add(NodeOrBuilder.of(child));
            return this;
        }

        public Node build() {
            return new Node(
                Optional.ofNullable(name),
                Optional.ofNullable(model),
                Optional.ofNullable(skeleton),
                children.stream().map(NodeOrBuilder::toNode).toList(),
                matrix);
        }

        @Override
        public String toString() {
            return "Node.Builder{" +
                "children=" + children +
                ", name='" + name + '\'' +
                ", model=" + model +
                ", skeleton=" + skeleton +
                ", matrix=" + matrix +
                '}';
        }
    }
}
