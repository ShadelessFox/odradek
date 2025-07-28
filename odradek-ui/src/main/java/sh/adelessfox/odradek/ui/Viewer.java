package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.Reflections;

import javax.swing.*;
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
        return Reflections.getGenericInterface(getClass(), Viewer.class)
            .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
            .orElseThrow();
    }
}
