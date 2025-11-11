package sh.adelessfox.odradek.scene;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.List;

public record Scene(List<Node> nodes) {
    public Scene {
        nodes = List.copyOf(nodes);
    }

    public static Scene of(Node node) {
        return new Scene(List.of(node));
    }

    public BoundingBox computeBoundingBox() {
        BoundingBox bbox = BoundingBox.empty();
        for (Node node : nodes) {
            bbox = bbox.union(node.computeBoundingBox());
        }
        return bbox;
    }
}
