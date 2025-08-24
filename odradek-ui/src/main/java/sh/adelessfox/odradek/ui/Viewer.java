package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.util.Reflections;

import javax.swing.*;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Viewer<T> {
    static Stream<Viewer<?>> viewers() {
        class Holder {
            static final List<Viewer<?>> viewers = ServiceLoader.load(Viewer.class).stream()
                .map(x -> (Viewer<?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.viewers.stream();
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
