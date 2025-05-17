package sh.adelessfox.odradek.geometry;

import java.util.List;
import java.util.Optional;

public record Mesh(Optional<String> name, List<Primitive> primitives) {
    public Mesh {
        primitives = List.copyOf(primitives);
    }

    public static Mesh of(List<Primitive> primitives) {
        return new Mesh(Optional.empty(), primitives);
    }

    public Mesh withName(String name) {
        return new Mesh(Optional.of(name), primitives);
    }
}
