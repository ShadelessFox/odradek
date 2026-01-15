package sh.adelessfox.odradek.ui;

import sh.adelessfox.odradek.game.Game;
import sh.adelessfox.odradek.rtti.ClassAttrInfo;
import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.ui.components.StyledText;
import sh.adelessfox.odradek.util.Gatherers;
import sh.adelessfox.odradek.util.Reflections;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides an inline renderer for objects show in the object tree.
 * <p>
 * It is expected to operate over raw game assets as it must
 * be as performant as possible.
 *
 * @param <T> the type of the object this renderer operates on
 * @param <G> the type of the game
 */
public interface Renderer<T, G extends Game> {
    interface OfObject<T, G extends Game> extends Renderer<T, G> {
        /**
         * Checks whether this renderer is applicable to the given type.
         * <p>
         * The default implementation checks whether the given {@code info}
         * is assignable to {@link T}.
         *
         * @param info type info
         * @return {@code true} if this renderer supports the given type
         */
        default boolean supports(TypeInfo info) {
            return supportedType().isAssignableFrom(info.type());
        }

        @SuppressWarnings("unchecked")
        default Class<T> supportedType() {
            return Reflections.getGenericInterface(getClass(), OfObject.class)
                .map(iface -> (Class<T>) Reflections.getRawType(iface.getActualTypeArguments()[0]))
                .orElseThrow();
        }
    }

    interface OfAttribute<T, G extends Game> extends Renderer<T, G> {
        /**
         * Checks whether this renderer is applicable to the given class attribute.
         * <p>
         * Keep in mind that all methods that accept {@link T} will
         * retrieve an instance of the class, not the attribute value.
         *
         * @param info class type info
         * @param attr class attribute info
         * @return {@code true} if this renderer supports the given attribute
         */
        default boolean supports(ClassTypeInfo info, ClassAttrInfo attr) {
            return false;
        }
    }

    static Stream<Renderer<?, ?>> renderers() {
        class Holder {
            static final List<Renderer<?, ?>> renderers = ServiceLoader.load(Renderer.class).stream()
                .map(x -> (Renderer<?, ?>) x.get())
                .collect(Collectors.toUnmodifiableList());
        }
        return Holder.renderers.stream();
    }

    @SuppressWarnings("unchecked")
    static <T, G extends Game> Optional<OfObject<T, G>> renderer(TypeInfo info) {
        return renderers()
            .gather(Gatherers.instanceOf(OfObject.class))
            .filter(r -> r.supports(info))
            .map(r -> (OfObject<T, G>) r)
            .findFirst();
    }

    @SuppressWarnings("unchecked")
    static <T, G extends Game> Optional<OfAttribute<T, G>> renderer(ClassTypeInfo parent, ClassAttrInfo attr) {
        return renderers()
            .gather(Gatherers.instanceOf(OfAttribute.class))
            .filter(r -> r.supports(parent, attr))
            .map(r -> (OfAttribute<T, G>) r)
            .findFirst();
    }

    /**
     * Retrieves the text that is shown as a label the object tree.
     * <p>
     * This method is called every time the tree is redrawn. It must not
     * perform any expensive operations as this might freeze the UI.
     * <p>
     * The default implementation returns {@link Optional#empty()}.
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
     * <p>
     * The default implementation returns {@link Optional#empty()}.
     *
     * @param info   the type of the {@code object}
     * @param object the object to display the text for
     * @param game   the associated game
     * @return a styled text, that can be empty, or {@link Optional#empty()} in which case
     * {@link #text(TypeInfo, Object, Game)} will be called instead
     */
    default Optional<StyledText> styledText(TypeInfo info, T object, G game) {
        return Optional.empty();
    }
}
