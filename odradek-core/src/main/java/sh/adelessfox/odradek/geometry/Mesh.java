package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.collect.Ints;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.util.Check;

import java.util.List;
import java.util.Optional;

public record Mesh(
    Ints indices,
    Floats positions,
    Optional<Floats> normals,
    Optional<Floats> tangents,
    List<Floats> texCoords,
    List<Bytes> colors,
    Optional<Weights> weights
) {
    public record Weights(Floats values, Ints joints, int maxInfluence) {
        public Weights {
            if (values.length() != joints.length()) {
                throw new IllegalArgumentException("weights values and joints must have the same length");
            }
            if (maxInfluence <= 0) {
                throw new IllegalArgumentException("maximum influence must be greater than 0");
            }
        }
    }

    public Mesh {
        int vertices = positions.length() / 3;
        normals.ifPresent(floats -> check(floats.length(), vertices, 3));
        tangents.ifPresent(floats -> check(floats.length(), vertices, 4));
        texCoords.forEach(floats -> check(floats.length(), vertices, 2));
        colors.forEach(bytes -> check(bytes.length(), vertices, 4));
        weights.ifPresent(w -> {
            check(w.values.length(), vertices, w.maxInfluence);
            check(w.joints.length(), vertices, w.maxInfluence);
        });

        texCoords = List.copyOf(texCoords);
        colors = List.copyOf(colors);
    }

    private static void check(int length, int count, int elementSize) {
        Check.argument(length % elementSize == 0, "array length must be a multiple of elementSize");
        Check.argument(length == count * elementSize, "array length must be equal to count * elementSize");
    }

    public Bounds computeBounds() {
        var builder = Bounds.builder();

        for (int i = 0; i < indices().length(); i++) {
            int index = indices.get(i);
            float x = positions.get(index * 3/**/);
            float y = positions.get(index * 3 + 1);
            float z = positions.get(index * 3 + 2);

            builder.add(x, y, z);
        }

        return builder.build();
    }
}
