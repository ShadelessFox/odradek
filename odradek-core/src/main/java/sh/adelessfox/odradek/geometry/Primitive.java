package sh.adelessfox.odradek.geometry;

import sh.adelessfox.odradek.math.BoundingBox;

import java.util.Map;

public record Primitive(Accessor indices, Map<Semantic, Accessor> vertices, int hash) {
    public Primitive {
        if (indices.componentType() != ComponentType.UNSIGNED_SHORT && indices.componentType() != ComponentType.UNSIGNED_INT) {
            throw new IllegalArgumentException("indices must be of type UNSIGNED_SHORT or UNSIGNED_INT");
        }
        if (indices.elementType() != ElementType.SCALAR) {
            throw new IllegalArgumentException("indices must be of element type SCALAR");
        }
        if (indices.normalized()) {
            throw new IllegalArgumentException("indices cannot be normalized");
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
        var positions = positions();
        var view = positions.asFloatView();
        var bbox = BoundingBox.empty();

        for (int i = 0; i < positions.count(); i++) {
            float x = view.get(i, 0);
            float y = view.get(i, 1);
            float z = view.get(i, 2);
            bbox = bbox.encapsulate(x, y, z);
        }

        return bbox;
    }
}
