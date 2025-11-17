package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.geometry.ComponentType;
import sh.adelessfox.odradek.geometry.ElementType;

public record VertexAttribute(
    int location,
    ElementType elementType,
    ComponentType componentType,
    int offset,
    int stride,
    boolean normalized
) {
    public VertexAttribute {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }
        if (stride <= 0) {
            throw new IllegalArgumentException("stride must be positive");
        }
        if (normalized && (componentType == ComponentType.FLOAT || componentType == ComponentType.HALF_FLOAT)) {
            throw new IllegalArgumentException("normalized can only be used with integer component types");
        }
    }
}
