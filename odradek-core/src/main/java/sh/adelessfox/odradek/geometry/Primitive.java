package sh.adelessfox.odradek.geometry;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.Map;

public record Primitive(Accessor indices, Map<Semantic, Accessor> vertices, int hash) {
    public Primitive {
        if (indices.type().normalized()) {
            throw new IllegalArgumentException("indices must not be normalized");
        }
        if (!indices.type().unsigned()) {
            throw new IllegalArgumentException("indices must be unsigned");
        }
        if (!(indices.type() instanceof Type.I8) &&
            !(indices.type() instanceof Type.I16) &&
            !(indices.type() instanceof Type.I32)
        ) {
            throw new IllegalArgumentException("indices must be of type I8, I16, or I32");
        }
        if (!vertices.containsKey(Semantic.POSITION)) {
            throw new IllegalArgumentException("vertices must contain POSITION semantic");
        }
        vertices = Map.copyOf(vertices);
    }

    public Accessor positions() {
        return vertices.get(Semantic.POSITION);
    }

    public BoundingBox computeBoundingBox() {
        var indices = indices().asIntView();
        var positions = positions().asFloatView();
        var bbox = BoundingBox.empty();

        for (int i = 0; i < indices().count(); i++) {
            int index = indices.get(i, 0);
            float x = positions.get(index, 0);
            float y = positions.get(index, 1);
            float z = positions.get(index, 2);

            bbox = bbox.encapsulate(x, y, z);
        }

        return bbox;
    }
}
