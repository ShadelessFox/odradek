package sh.adelessfox.odradek.rtti.generator;

import wtf.reversed.toolbox.collect.*;

import java.util.Map;
import java.util.function.IntFunction;

public final class PrimitiveContainers {
    private static final Map<String, Representation> TYPES = Map.of(
        "byte", new Representation(Bytes.class, Bytes::allocate),
        "short", new Representation(Shorts.class, Shorts::allocate),
        "int", new Representation(Ints.class, Ints::allocate),
        "long", new Representation(Longs.class, Longs::allocate),
        "float", new Representation(Floats.class, Floats::allocate),
        "double", new Representation(Doubles.class, Doubles::allocate),
        "char", new Representation(Chars.class, Chars::allocate),
        "boolean", new Representation(Booleans.class, Booleans::allocate)
    );

    private PrimitiveContainers() {
    }

    public static Class<? extends Slice> wrapperType(String primitiveName) {
        return representation(primitiveName).type();
    }

    static Slice allocate(Class<?> primitive, int length) {
        return representation(primitive.getName()).allocator().apply(length);
    }

    private static Representation representation(String primitiveName) {
        var representation = TYPES.get(primitiveName);
        if (representation == null) {
            throw new IllegalArgumentException("Unsupported container primitive: " + primitiveName);
        }
        return representation;
    }

    private record Representation(Class<? extends Slice> type, IntFunction<? extends Slice> allocator) {
    }
}
