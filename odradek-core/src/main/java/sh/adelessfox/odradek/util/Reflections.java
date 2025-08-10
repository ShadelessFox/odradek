package sh.adelessfox.odradek.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.stream.Stream;

public final class Reflections {
    private Reflections() {
    }

    public static Optional<ParameterizedType> getGenericInterface(Class<?> cls, Class<?> iface) {
        return Stream.of(cls.getGenericInterfaces())
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType() == iface)
            .findFirst();
    }

    public static Class<?> getRawType(Type type) {
        return switch (type) {
            case Class<?> cls -> cls;
            case ParameterizedType pt -> (Class<?>) pt.getRawType();
            default -> throw new IllegalArgumentException(type.toString());
        };
    }
}
