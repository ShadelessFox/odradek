package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.util.Reflections;

import javax.swing.*;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A graphical viewer for an object of the given type. Viewers are shown
 * as a dedicated panel in the UI with which the user can interact.
 * <p>
 * Implementations should always prefer intermediate types obtained
 * via {@link sh.adelessfox.odradek.game.Converter} rather than
 * game-specific, unless a viewer is game-specific as well.
 */
public interface Viewer extends Activable, Disposable {
    interface Provider<T> {
        Viewer create(T object, Game game);

        String name();

        default Optional<String> icon() {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        default Class<T> supportedType() {
            return Reflections.getGenericInterface(getClass(), Viewer.Provider.class)
                .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
                .orElseThrow();
        }
    }

    static Stream<Provider<?>> providers() {
        class Holder {
            static final List<Provider<?>> viewers = ServiceLoader.load(Provider.class).stream()
                .map(x -> (Provider<?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.viewers.stream();
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<Provider<T>> providers(Class<T> cls) {
        return providers()
            .filter(v -> v.supportedType().isAssignableFrom(cls))
            .map(v -> (Provider<T>) v);
    }

    JComponent createComponent();

    @Override
    default void activate() {
        // do nothing by default
    }

    @Override
    default void deactivate() {
        // do nothing by default
    }

    @Override
    default void dispose() {
        // do nothing by default
    }
}
