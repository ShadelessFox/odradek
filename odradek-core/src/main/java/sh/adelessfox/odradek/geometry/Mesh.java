package sh.adelessfox.odradek.geometry;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.List;
import java.util.Optional;

public record Mesh(Optional<String> name, List<Primitive> primitives) {
    public Mesh {
        primitives = List.copyOf(primitives);
    }

    public static Mesh of(List<Primitive> primitives) {
        return new Mesh(Optional.empty(), primitives);
    }

    public BoundingBox computeBoundingBox() {
        var bbox = BoundingBox.empty();
        for (var primitive : primitives) {
            bbox = bbox.encapsulate(primitive.computeBoundingBox());
        }
        return bbox;
    }
}
