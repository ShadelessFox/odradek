package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Model;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;

import java.util.*;
import java.util.stream.Stream;

public record Node(
    Optional<String> name,
    Optional<Model> model,
    Optional<Skin> skin,
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
        return new Node(name, model, skin, children, matrix);
    }

    public Node transform(Matrix4 transform) {
        return new Node(name, model, skin, children, matrix.multiply(transform));
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

    public Optional<Bounds> computeBounds() {
        var bbox1 = model.stream()
            .map(Model::computeBounds);

        var bbox2 = children.stream()
            .map(Node::computeBounds)
            .flatMap(Optional::stream);

        return Stream.concat(bbox1, bbox2)
            .reduce(Bounds::combine)
            .map(bbox -> bbox.transform(matrix));
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
        private Skin skin;
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

        public Builder skin(Skin skin) {
            this.skin = skin;
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
                Optional.ofNullable(skin),
                children.stream().map(NodeOrBuilder::toNode).toList(),
                matrix);
        }

        @Override
        public String toString() {
            return "Node.Builder{" +
                "children=" + children +
                ", name='" + name + '\'' +
                ", model=" + model +
                ", skin=" + skin +
                ", matrix=" + matrix +
                '}';
        }
    }
}
