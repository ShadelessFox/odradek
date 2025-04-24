package sh.adelessfox.odradek.geometry;

import java.util.List;
import java.util.Optional;

public record Mesh(Optional<String> name, List<Primitive> primitives) {
    public Mesh {
        primitives = List.copyOf(primitives);
    }
}
