package sh.adelessfox.odradek.ui;

import javax.swing.*;
import java.lang.reflect.ParameterizedType;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Viewer<T> {
    static Stream<Viewer<?>> viewers() {
        return ServiceLoader.load(Viewer.class).stream().map(x -> (Viewer<?>) x.get());
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<Viewer<T>> viewers(Class<T> clazz) {
        return viewers()
            .filter(v -> v.supportedType().isAssignableFrom(clazz))
            .map(v -> (Viewer<T>) v);
    }

    JComponent createPreview(T object);

    String displayName();

    @SuppressWarnings("unchecked")
    default Class<T> supportedType() {
        return Stream.of(getClass().getGenericInterfaces())
            .map(ParameterizedType.class::cast)
            .filter(type -> type.getRawType() == Viewer.class)
            .map(type -> (Class<T>) type.getActualTypeArguments()[0])
            .findFirst().orElseThrow();
    }
}
