package sh.adelessfox.odradek.opengl;

import sh.adelessfox.odradek.geometry.Type;

public record VertexAttribute(
    int location,
    Type type,
    int offset,
    int stride
) {
    public VertexAttribute {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }
        if (stride <= 0) {
            throw new IllegalArgumentException("stride must be positive");
        }
    }
}
