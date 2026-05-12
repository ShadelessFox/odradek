package sh.adelessfox.odradek.game.decima;

import sh.adelessfox.odradek.rtti.ClassTypeInfo;
import sh.adelessfox.odradek.rtti.data.TypedObject;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Per-game registry that identifies "top-level container" object types and resolves
 * display names for named objects (e.g. TextureSet).
 * <p>
 * In Decima archives most object names are stripped at build time; only a handful of
 * types (notably TextureSet) carry surviving names. To locate a model from a named
 * texture, a reverse traversal stops once it reaches a "container" type that groups
 * a renderable model. This SPI lets each game define its own set of container types.
 */
public interface ContainerTypeRegistry {
    /** Returns true if this registry applies to the given game. */
    boolean supports(DecimaGame game);

    /**
     * Type names (as reported by {@link ClassTypeInfo#name()}) that are considered
     * top-level model containers — reverse BFS terminates when it reaches one.
     */
    Set<String> containerTypeNames();

    /**
     * Best-effort display name for an object. Returns empty if the object has no
     * surviving name (which is the common case).
     */
    Optional<String> nameOf(TypedObject object);

    default boolean isContainer(ClassTypeInfo type) {
        return containerTypeNames().contains(type.name());
    }

    static Optional<ContainerTypeRegistry> lookup(DecimaGame game) {
        return ServiceLoader.load(ContainerTypeRegistry.class).stream()
            .map(ServiceLoader.Provider::get)
            .filter(r -> r.supports(game))
            .findFirst();
    }
}
