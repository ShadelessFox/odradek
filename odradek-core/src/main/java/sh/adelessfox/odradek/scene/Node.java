package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.math.Mat4;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Node(Optional<String> name, Optional<Mesh> mesh, Optional<Node> skin, List<Node> children, Mat4 matrix) {
    public Node {
        children = List.copyOf(children);
    }

    public static Builder builder() {
        return new Builder();
    }


    public static Node of(List<Node> children) {
        return new Node(Optional.empty(), Optional.empty(), Optional.empty(), children, Mat4.identity());
    }

    public static Node of(Mesh mesh) {
        return new Node(Optional.empty(), Optional.of(mesh), Optional.empty(), List.of(), Mat4.identity());
    }

    public Node add(Node child) {
        var children = new ArrayList<>(this.children);
        children.add(child);
        return new Node(name, mesh, skin, children, matrix);
    }

    public Node transform(Mat4 transform) {
        return new Node(name, mesh, skin, children, matrix.mul(transform));
    }

    public static final class Builder {
        private final List<Node> children = new ArrayList<>();
        private String name;
        private Mesh mesh;
        private Node skin;
        private Mat4 matrix = Mat4.identity();

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

        public Builder matrix(Mat4 matrix) {
            this.matrix = matrix;
            return this;
        }

        public Builder add(Node child) {
            children.add(child);
            return this;
        }

        public Node build() {
            return new Node(Optional.ofNullable(name), Optional.ofNullable(mesh), Optional.ofNullable(skin), List.copyOf(children), matrix);
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
