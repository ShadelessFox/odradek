package sh.adelessfox.odradek.geometry;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.List;
import java.util.Optional;

public record Mesh(Optional<String> name, List<Primitive> primitives) {
    public Mesh {
        if (primitives.isEmpty()) {
            throw new IllegalArgumentException("mesh must consist of at least one primitive");
        }
        primitives = List.copyOf(primitives);
    }

    public static Mesh of(List<Primitive> primitives) {
        return new Mesh(Optional.empty(), primitives);
    }

    public BoundingBox computeBoundingBox() {
        return primitives.stream()
            .map(Primitive::computeBoundingBox)
            .reduce(BoundingBox::encapsulate)
            .orElseThrow();
    }
}
