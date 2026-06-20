package sh.adelessfox.odradek.geometry;

import wtf.reversed.toolbox.collect.Bytes;
import wtf.reversed.toolbox.collect.Floats;
import wtf.reversed.toolbox.collect.Ints;
import wtf.reversed.toolbox.math.Bounds;
import wtf.reversed.toolbox.util.Check;

import java.util.List;
import java.util.Optional;

/**
 * Defines a 3D triangle mesh that consists of vertex attributes and triangle indices.
 *
 * @param indices   the triangle indices, where every three consecutive integers define a triangle.
 *                  The winding order is counter-clockwise for front-facing triangles.
 * @param positions the vertex positions that define a vertex position (x, y, z).
 * @param normals   the vertex normals that define a vertex normal (x, y, z).
 * @param tangents  the vertex tangents that define a vertex tangent (x, y, z).
 * @param texCoords the vertex texture coordinates that define a set of vertex texture coordinates (u, v).
 *                  The origin of the texture coordinates (0, 0) corresponds to the upper left corner of a texture image.
 * @param colors    the vertex colors, where every four consecutive bytes define a vertex color (r, g, b, a).
 */
public record Mesh(
    Ints indices,
    Floats positions,
    Optional<Floats> normals,
    Optional<Floats> tangents,
    List<Floats> texCoords,
    List<Bytes> colors,
    Optional<Weights> weights
) {
    public record Weights(Floats values, Ints bones, int maxInfluence) {
        public Weights {
            if (values.length() != bones.length()) {
                throw new IllegalArgumentException("weights values and bones must have the same length");
            }
            if (maxInfluence <= 0) {
                throw new IllegalArgumentException("maximum influence must be greater than 0");
            }
        }
    }

    public Mesh {
        int vertices = positions.length() / 3;
        normals.ifPresent(floats -> check(floats.length(), vertices, 3));
        tangents.ifPresent(floats -> check(floats.length(), vertices, 3));
        texCoords.forEach(floats -> check(floats.length(), vertices, 2));
        colors.forEach(bytes -> check(bytes.length(), vertices, 4));
        weights.ifPresent(w -> {
            check(w.values.length(), vertices, w.maxInfluence);
            check(w.bones.length(), vertices, w.maxInfluence);
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
