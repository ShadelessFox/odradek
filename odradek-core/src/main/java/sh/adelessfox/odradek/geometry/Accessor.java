package sh.adelessfox.odradek.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public record Accessor(
    ByteBuffer buffer,
    ElementType elementType,
    ComponentType componentType,
    int offset,
    int count,
    int stride,
    boolean normalized
) {
    public Accessor {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be positive");
        }
        if (stride <= 0) {
            throw new IllegalArgumentException("stride must be positive");
        }
        if (normalized && (componentType == ComponentType.FLOAT || componentType == ComponentType.HALF_FLOAT)) {
            throw new IllegalArgumentException("normalized can only be used with integer component types");
        }
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("buffer must be LITTLE_ENDIAN");
        }
    }

    public Accessor(ByteBuffer buffer, ElementType elementType, ComponentType componentType, int offset, int count, int stride) {
        this(buffer, elementType, componentType, offset, count, stride, false);
    }

    public Accessor(ByteBuffer buffer, ElementType elementType, ComponentType componentType, int offset, int count) {
        this(buffer, elementType, componentType, offset, count, elementType.size() * componentType.size());
    }

    public int componentCount() {
        return elementType().size();
    }

    public ByteView asByteView() {
        return switch (componentType) {
            case BYTE, UNSIGNED_BYTE -> ByteView.ofByte(this, buffer);
            default -> throw new UnsupportedOperationException("Unsupported component type: " + componentType);
        };
    }

    public ShortView asShortView() {
        return switch (componentType) {
            case SHORT, UNSIGNED_SHORT -> ShortView.ofShort(this, buffer);
            default -> throw new UnsupportedOperationException("Unsupported component type: " + componentType);
        };
    }

    public IntView asIntView() {
        return switch (componentType) {
            case SHORT, UNSIGNED_SHORT -> IntView.ofShort(this, buffer);
            case INT, UNSIGNED_INT -> IntView.ofInt(this, buffer);
            default -> throw new UnsupportedOperationException("Unsupported component type: " + componentType);
        };
    }

    public FloatView asFloatView() {
        return switch (componentType) {
            case FLOAT -> FloatView.ofFloat(this, buffer);
            case HALF_FLOAT -> FloatView.ofHalfFloat(this, buffer);
            case UNSIGNED_SHORT -> FloatView.ofUnsignedShort(this, buffer);
            case SHORT -> FloatView.ofShort(this, buffer);
            case INT_10_10_10_2 -> FloatView.ofX10Y10Z10W2(this, buffer);
            default -> throw new UnsupportedOperationException("Unsupported component type: " + componentType);
        };
    }

    private int getPositionFor(int elementIndex, int componentIndex) {
        Objects.checkIndex(elementIndex, count);
        Objects.checkIndex(componentIndex, elementType.size());

        return offset + (elementIndex * stride) + (componentIndex * componentType.size());
    }

    public interface ByteView {
        static ByteView ofByte(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> buffer.get(accessor.getPositionFor(e, c));
        }

        byte get(int elementIndex, int componentIndex);
    }

    public interface ShortView {
        static ShortView ofShort(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> buffer.getShort(accessor.getPositionFor(e, c));
        }

        short get(int elementIndex, int componentIndex);
    }

    public interface IntView {
        static IntView ofShort(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> Short.toUnsignedInt(buffer.getShort(accessor.getPositionFor(e, c)));
        }

        static IntView ofInt(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> buffer.getInt(accessor.getPositionFor(e, c));
        }

        int get(int elementIndex, int componentIndex);
    }

    public interface FloatView {
        static FloatView ofFloat(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> buffer.getFloat(accessor.getPositionFor(e, c));
        }

        static FloatView ofHalfFloat(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> Float.float16ToFloat(buffer.getShort(accessor.getPositionFor(e, c)));
        }

        static FloatView ofShort(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> buffer.getShort(accessor.getPositionFor(e, c)) / 32767f;
        }

        static FloatView ofUnsignedShort(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> Short.toUnsignedInt(buffer.getShort(accessor.getPositionFor(e, c))) / 65535f;
        }

        static FloatView ofX10Y10Z10W2(Accessor accessor, ByteBuffer buffer) {
            return (e, c) -> {
                var value = buffer.getInt(accessor.getPositionFor(e, 0));

                // Division by 510 gives a smaller error than by 511. How come?
                var x = (value << 22 >> 22) / 510f;
                var y = (value << 12 >> 22) / 510f;
                var z = (value << 2 >> 22) / 510f;
                var w = (value >> 30);
                var length = (float) Math.sqrt(x * x + y * y + z * z);

                return switch (c) {
                    case 0 -> x / length;
                    case 1 -> y / length;
                    case 2 -> z / length;
                    case 3 -> w;
                    default -> Objects.checkIndex(c, 4);
                };
            };
        }

        float get(int elementIndex, int componentIndex);
    }
}
