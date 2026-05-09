package sh.adelessfox.odradek.scene;

import wtf.reversed.toolbox.math.Bounds;

import java.util.List;
import java.util.Optional;

public record Scene(List<Node> nodes) {
    public Scene {
        nodes = List.copyOf(nodes);
    }

    public static Scene of(Node node) {
        return new Scene(List.of(node));
    }

    public void accept(NodeVisitor visitor) {
        for (var node : nodes) {
            node.accept(visitor);
        }
    }

    public Optional<Bounds> computeBounds() {
        return nodes.stream()
            .map(Node::computeBounds)
            .flatMap(Optional::stream)
            .reduce(Bounds::combine);
    }
}
