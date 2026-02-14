package sh.adelessfox.odradek.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Objects;

public sealed interface Accessor {
    /**
     * Creates a new accessor backed by the given buffer.
     *
     * @param buffer the buffer to read from
     * @param offset the offset in bytes from the start of the buffer to the first element
     * @param stride the stride in bytes between elements. Can be {@code 0} for tightly packed data
     * @param type   the type of the elements in the buffer
     * @param count  the number of elements in the buffer
     * @return a new accessor backed by the given buffer
     */
    static Accessor of(ByteBuffer buffer, int offset, int stride, Type type, int count) {
        Objects.checkIndex(offset, buffer.remaining());
        if (buffer.order() != ByteOrder.LITTLE_ENDIAN) {
            throw new IllegalArgumentException("buffer must be LITTLE_ENDIAN");
        }
        var slice = buffer.slice(buffer.position() + offset, count * stride - offset);
        return new OfBuffer(slice, stride, type, count);
    }

    /**
     * Creates a new accessor by interleaving the given accessors.
     * <p>
     * The accessors must have the same type and count.
     * <p>
     * Consider a case with two accessors of type {@code I32[4]} and {@code I32[2]}, respectively.
     * By interleaving them, a new accessor of type {@code I32[6]} will be created, where
     * the first {@code 4} components come from the first accessor and the last {@code 2} components
     * come from the second accessor.
     *
     * @param accessors the accessors to interleave; must not be empty
     * @return a new accessor that interleaves the given accessors
     */
    static Accessor ofInterleaved(List<? extends Accessor> accessors) {
        if (accessors.isEmpty()) {
            throw new IllegalArgumentException("accessors cannot be empty");
        }
        var type = accessors.getFirst().type();
        var count = accessors.getFirst().count();
        return new OfInterleaved(accessors, type, count);
    }

    Type type();

    int count();

    default ByteView asByteView() {
        return switch (type()) {
            case Type.I8 x -> ByteView.of(this, x);
            default -> throw new UnsupportedOperationException("Unsupported type for byte view: " + type());
        };
    }

    default ShortView asShortView() {
        return switch (type()) {
            case Type.I8 x -> ShortView.of(this, x);
            case Type.I16 x -> ShortView.of(this, x);
            default -> throw new UnsupportedOperationException("Unsupported type for short view: " + type());
        };
    }

    default IntView asIntView() {
        return switch (type()) {
            case Type.I8 x -> IntView.of(this, x);
            case Type.I16 x -> IntView.of(this, x);
            case Type.I32 x -> IntView.of(this, x);
            default -> throw new UnsupportedOperationException("Unsupported type for int view: " + type());
        };
    }

    default FloatView asFloatView() {
        return switch (type()) {
            case Type.I8 x -> FloatView.of(this, x);
            case Type.I16 x -> FloatView.of(this, x);
            case Type.I32 x -> FloatView.of(this, x);
            case Type.F16 x -> FloatView.of(this, x);
            case Type.F32 x -> FloatView.of(this, x);
            case Type.X10Y10Z10W2 x -> FloatView.of(this, x);
        };
    }

    byte getByte(int elementIndex, int componentIndex);

    short getShort(int elementIndex, int componentIndex);

    int getInt(int elementIndex, int componentIndex);

    float getFloat(int elementIndex, int componentIndex);

    default int componentCount() {
        return type().components();
    }

    record OfBuffer(ByteBuffer buffer, int stride, Type type, int count) implements Accessor {
        public OfBuffer {
            buffer = buffer
                .asReadOnlyBuffer()
                .order(ByteOrder.LITTLE_ENDIAN);
        }

        @Override
        public byte getByte(int elementIndex, int componentIndex) {
            return buffer.get(positionFor(elementIndex, componentIndex));
        }

        @Override
        public short getShort(int elementIndex, int componentIndex) {
            return buffer.getShort(positionFor(elementIndex, componentIndex));
        }

        @Override
        public int getInt(int elementIndex, int componentIndex) {
            return buffer.getInt(positionFor(elementIndex, componentIndex));
        }

        @Override
        public float getFloat(int elementIndex, int componentIndex) {
            return buffer.getFloat(positionFor(elementIndex, componentIndex));
        }

        private int positionFor(int elementIndex, int componentIndex) {
            Objects.checkIndex(elementIndex, count);
            Objects.checkIndex(componentIndex, type.components());

            return (elementIndex * stride) + (componentIndex * type.byteSize());
        }
    }

    record OfInterleaved(List<? extends Accessor> accessors, Type type, int count) implements Accessor {
        public OfInterleaved {
            if (accessors.isEmpty()) {
                throw new IllegalArgumentException("accessors cannot be empty");
            }
            var first = accessors.getFirst();
            if (first.type() != type) {
                throw new IllegalArgumentException("type must match the type of the accessors");
            }
            if (first.count() != count) {
                throw new IllegalArgumentException("count must match the count of the accessors");
            }
            int components = 0;
            for (var accessor : accessors) {
                if (accessor.type().getClass() != first.type().getClass()
                    || accessor.type().normalized() != first.type().normalized()
                    || accessor.type().unsigned() != first.type().unsigned()
                ) {
                    throw new IllegalArgumentException("all accessors must have the same type");
                }
                if (accessor.count() != first.count()) {
                    throw new IllegalArgumentException("all accessors must have the same count");
                }
                components += accessor.type().components();
            }
            accessors = List.copyOf(accessors);
            type = type.withComponents(components);
        }

        @Override
        public byte getByte(int elementIndex, int componentIndex) {
            return accessorFor(componentIndex).getByte(elementIndex, componentIndex % 4);
        }

        @Override
        public short getShort(int elementIndex, int componentIndex) {
            return accessorFor(componentIndex).getShort(elementIndex, componentIndex % 4);
        }

        @Override
        public int getInt(int elementIndex, int componentIndex) {
            return accessorFor(componentIndex).getInt(elementIndex, componentIndex % 4);
        }

        @Override
        public float getFloat(int elementIndex, int componentIndex) {
            return accessorFor(componentIndex).getFloat(elementIndex, componentIndex % 4);
        }

        private Accessor accessorFor(int componentIndex) {
            return accessors.get(componentIndex / 4);
        }
    }

    @FunctionalInterface
    interface ByteView {
        @SuppressWarnings("unused")
        static ByteView of(Accessor accessor, Type.I8 type) {
            return accessor::getByte;
        }

        byte get(int elementIndex, int componentIndex);
    }

    @FunctionalInterface
    interface ShortView {
        static ShortView of(Accessor accessor, Type.I8 type) {
            if (type.unsigned()) {
                return (e, c) -> (short) Byte.toUnsignedInt(accessor.getByte(e, c));
            } else {
                return accessor::getByte;
            }
        }

        @SuppressWarnings("unused")
        static ShortView of(Accessor accessor, Type.I16 type) {
            return accessor::getShort;
        }

        short get(int elementIndex, int componentIndex);
    }

    @FunctionalInterface
    interface IntView {
        static IntView of(Accessor accessor, Type.I8 type) {
            if (type.unsigned()) {
                return (e, c) -> Byte.toUnsignedInt(accessor.getByte(e, c));
            } else {
                return accessor::getByte;
            }
        }

        static IntView of(Accessor accessor, Type.I16 type) {
            if (type.unsigned()) {
                return (e, c) -> Short.toUnsignedInt(accessor.getShort(e, c));
            } else {
                return accessor::getShort;
            }
        }

        @SuppressWarnings("unused")
        static IntView of(Accessor accessor, Type.I32 type) {
            return accessor::getInt;
        }

        int get(int elementIndex, int componentIndex);
    }

    @FunctionalInterface
    interface FloatView {
        static FloatView of(Accessor accessor, Type.I8 type) {
            if (type.unsigned()) {
                return (e, c) -> Byte.toUnsignedInt(accessor.getByte(e, c)) / 255f;
            } else {
                return (e, c) -> accessor.getByte(e, c) / 127f;
            }
        }

        static FloatView of(Accessor accessor, Type.I16 type) {
            if (type.unsigned()) {
                return (e, c) -> Short.toUnsignedInt(accessor.getShort(e, c)) / 65535f;
            } else {
                return (e, c) -> accessor.getShort(e, c) / 32767f;
            }
        }

        static FloatView of(Accessor accessor, Type.I32 type) {
            if (type.unsigned()) {
                return (e, c) -> Integer.toUnsignedLong(accessor.getInt(e, c)) / 4294967295f;
            } else {
                return (e, c) -> accessor.getInt(e, c) / 2147483647f;
            }
        }

        @SuppressWarnings("unused")
        static FloatView of(Accessor accessor, Type.F16 type) {
            return (e, c) -> Float.float16ToFloat(accessor.getShort(e, c));
        }

        @SuppressWarnings("unused")
        static FloatView of(Accessor accessor, Type.F32 type) {
            return accessor::getFloat;
        }

        static FloatView of(Accessor accessor, Type.X10Y10Z10W2 type) {
            if (!type.normalized()) {
                throw new UnsupportedOperationException("X10Y10Z10W2 type must be normalized for float view");
            }
            if (type.unsigned()) {
                throw new UnsupportedOperationException("X10Y10Z10W2 type must be signed for float view");
            }
            return (e, c) -> {
                var value = accessor.getInt(e, 0);

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
