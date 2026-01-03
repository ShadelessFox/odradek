package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.util.Reflections;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A graphical popup for an object of the given type. A preview is
 * shown as a popup when the user hovers over an item in the UI.
 * <p>
 * Implementations should always prefer intermediate types obtained
 * via {@link sh.adelessfox.odradek.game.Converter} rather than
 * game-specific, unless a preview is game-specific as well.
 */
public interface Preview {
    interface Provider<T> {
        Preview create(T object);

        @SuppressWarnings("unchecked")
        default Class<T> supportedType() {
            return Reflections.getGenericInterface(getClass(), Preview.Provider.class)
                .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
                .orElseThrow();
        }
    }

    static Stream<Provider<?>> providers() {
        class Holder {
            static final List<Provider<?>> previews = ServiceLoader.load(Provider.class).stream()
                .map(x -> (Provider<?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.previews.stream();
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<Provider<T>> provider(Class<T> cls) {
        return providers()
            .filter(p -> p.supportedType().isAssignableFrom(cls))
            .map(p -> (Provider<T>) p)
            .findFirst();
    }

    JComponent createComponent();
}
