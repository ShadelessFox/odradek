package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Matrix4f;

import java.util.*;
import java.util.stream.Stream;

public record Node(
    Optional<String> name,
    Optional<Mesh> mesh,
    Optional<Skin> skin,
    List<Node> children,
    Matrix4f matrix
) {
    public Node {
        children = List.copyOf(children);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Node of(List<Node> children) {
        return new Node(Optional.empty(), Optional.empty(), Optional.empty(), children, Matrix4f.identity());
    }

    public static Node of(Mesh mesh) {
        return new Node(Optional.empty(), Optional.of(mesh), Optional.empty(), List.of(), Matrix4f.identity());
    }

    public Node add(Node child) {
        var children = new ArrayList<>(this.children);
        children.add(child);
        return new Node(name, mesh, skin, children, matrix);
    }

    public Node transform(Matrix4f transform) {
        return new Node(name, mesh, skin, children, matrix.mul(transform));
    }

    public Optional<BoundingBox> computeBoundingBox() {
        var bbox1 = mesh.stream()
            .map(Mesh::computeBoundingBox);

        var bbox2 = children.stream()
            .map(Node::computeBoundingBox)
            .flatMap(Optional::stream);

        return Stream.concat(bbox1, bbox2)
            .reduce(BoundingBox::encapsulate)
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
        private Mesh mesh;
        private Skin skin;
        private Matrix4f matrix = Matrix4f.identity();

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder mesh(Mesh mesh) {
            this.mesh = mesh;
            return this;
        }

        public Builder skin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public Matrix4f matrix() {
            return matrix;
        }

        public Builder matrix(Matrix4f matrix) {
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
                Optional.ofNullable(mesh),
                Optional.ofNullable(skin),
                children.stream().map(NodeOrBuilder::toNode).toList(),
                matrix);
        }

        @Override
        public String toString() {
            return "Node.Builder{" +
                "children=" + children +
                ", name='" + name + '\'' +
                ", mesh=" + mesh +
                ", skin=" + skin +
                ", matrix=" + matrix +
                '}';
        }
    }
}
