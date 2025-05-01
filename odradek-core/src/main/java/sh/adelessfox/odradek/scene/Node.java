package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.geometry.Mesh;
import sh.adelessfox.odradek.math.Mat4;

import java.util.List;
import java.util.Optional;

public record Node(Optional<String> name, Optional<Mesh> mesh, List<Node> children, Mat4 matrix) {
    public Node {
        children = List.copyOf(children);
    }

    public Node(List<Node> children) {
        this(Optional.empty(), Optional.empty(), children, Mat4.identity());
    }

    public Node transform(Mat4 transform) {
        return new Node(name, mesh, children, matrix.mul(transform));
    }
}
