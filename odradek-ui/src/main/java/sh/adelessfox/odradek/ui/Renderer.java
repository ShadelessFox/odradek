package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.util.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an inline renderer for objects show in the object tree.
 * <p>
 * It is expected to operate over raw game assets.
 *
 * @param <T> the type of the object this renderer operates on
 * @param <G> the type of the game
 */
public interface Renderer<T, G extends Game> {
    static Stream<Renderer<?, ?>> renderers() {
        class Holder {
            static final List<Renderer<?, ?>> renderers = ServiceLoader.load(Renderer.class).stream()
                .map(x -> (Renderer<?, ?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.renderers.stream();
    }

    @SuppressWarnings("unchecked")
    static <T, G extends Game> Stream<Renderer<T, G>> renderers(TypeInfo info) {
        return renderers()
            .filter(r -> r.supports(info))
            .map(r -> (Renderer<T, G>) r);
    }

    @SuppressWarnings("unchecked")
    static <T, G extends Game> Optional<Renderer<T, G>> renderer(TypeInfo info) {
        return renderers(info)
            .findFirst()
            .map(r -> (Renderer<T, G>) r);
    }

    /**
     * Retrieves the text that is shown as a label the object tree.
     * <p>
     * This method is called every time the tree is redrawn. It must not
     * perform any expensive operations as this might freeze the UI.
     *
     * @param info   the type of the {@code object}
     * @param object the object to display the text for
     * @param game   the associated game
     * @return a text, that can be empty, or {@link Optional#empty()} if {@link Object#toString()} should be used instead
     */
    default Optional<String> text(TypeInfo info, T object, G game) {
        return Optional.empty();
    }

    /**
     * Similar to {@link #text(TypeInfo, Object, Game)}, but provides styling capabilities.
     *
     * @param info   the type of the {@code object}
     * @param object the object to display the text for
     * @param game   the associated game
     * @return a styled text, that can be empty, or {@link Optional#empty()} if {@link Object#toString()} should be used instead
     */
    default Optional<StyledText> styledText(TypeInfo info, T object, G game) {
        return Optional.empty();
    }

    default boolean supports(TypeInfo info) {
        return supportedType().isAssignableFrom(info.type());
    }

    @SuppressWarnings("unchecked")
    default Class<T> supportedType() {
        return Reflections.getGenericInterface(getClass(), Renderer.class)
            .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
            .orElseThrow();
    }
}
