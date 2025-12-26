package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.List;
import java.util.Optional;

public record Scene(List<Node> nodes) {
    public Scene {
        nodes = List.copyOf(nodes);
    }

    public static Scene of(Node node) {
        return new Scene(List.of(node));
    }

    public Optional<BoundingBox> computeBoundingBox() {
        return nodes.stream()
            .map(Node::computeBoundingBox)
            .flatMap(Optional::stream)
            .reduce(BoundingBox::encapsulate);
    }
}
