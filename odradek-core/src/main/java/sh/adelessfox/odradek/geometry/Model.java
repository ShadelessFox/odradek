package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.math.Bounds;

import java.util.List;
import java.util.Optional;

public record Model(Optional<String> name, List<Mesh> meshes) {
    public Model {
        if (meshes.isEmpty()) {
            throw new IllegalArgumentException("model must consist of at least one mesh");
        }
        meshes = List.copyOf(meshes);
    }

    public static Model of(List<Mesh> meshes) {
        return new Model(Optional.empty(), meshes);
    }

    public static Model of(Mesh mesh) {
        return of(List.of(mesh));
    }

    public Bounds computeBoundingBox() {
        return meshes.stream()
            .map(Mesh::computeBoundingBox)
            .reduce(Bounds::combine)
            .orElseThrow();
    }
}
