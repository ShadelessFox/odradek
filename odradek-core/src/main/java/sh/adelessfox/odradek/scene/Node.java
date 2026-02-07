package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.math.BoundingBox;
import sh.adelessfox.odradek.math.Matrix4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record Node(
    Optional<String> name,
    Optional<Mesh> mesh,
    Optional<Node> skin,
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

    public void accept(NodeVisitor visitor) {
        accept(visitor, matrix);
    }

    private void accept(NodeVisitor visitor, Matrix4f transform) {
        if (visitor.visit(this, transform)) {
            for (var child : children) {
                child.accept(visitor, transform.mul(child.matrix));
            }
        }
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
        private final List<Node> children = new ArrayList<>();
        private String name;
        private Mesh mesh;
        private Node skin;
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

        public Builder skin(Node skin) {
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
            this.children.addAll(children);
            return this;
        }

        public Builder add(Node child) {
            children.add(child);
            return this;
        }

        public Node build() {
            return new Node(Optional.ofNullable(name), Optional.ofNullable(mesh), Optional.ofNullable(skin), children, matrix);
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
