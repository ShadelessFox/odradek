package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.math.Matrix4;

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

    /**
     * Computes bounds in the space defined by {@code transform}.
     *
     * @param transform the complete model-local-to-result-space transform
     */
    public Optional<Bounds> computeBounds(Matrix4 transform) {
        return meshes.stream()
            .map(Mesh::computeBounds)
            .flatMap(Optional::stream)
            .map(bounds -> bounds.transform(transform))
            .reduce(Bounds::combine);
    }
}
